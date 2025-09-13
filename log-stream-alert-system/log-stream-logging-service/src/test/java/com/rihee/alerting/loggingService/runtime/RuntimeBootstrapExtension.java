package com.rihee.alerting.loggingService.runtime;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import com.rihee.alerting.loggingService.testinfra.common.Proc;
import com.rihee.alerting.loggingService.testinfra.common.TestProcessorAdapter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 확장: 런타임 파이프라인을 한 번만 초기화하고, 각 테스트에 테스트 어댑터를 주입/정리한다.
 *
 * <p><b>역할</b>
 * <ul>
 *   <li><b>초기화(1회):</b>
 *      {@link SettingLoader}에서 세팅을 로딩해서 가져온 {@literal List<LogProcessorPort>} 결과를
 *      {@code Map<Class<? extends LogProcessorPort>, LogProcessorPort>}로 캐시한다.</li>
 *   <li><b>주입:</b> 테스트 메서드 파라미터에 선언된
 *       {@link com.rihee.alerting.loggingService.testinfra.common.Proc @Proc}(타입)를 해석해
 *       대응하는 {@link TestProcessorAdapter}를 찾아 반환한다.
 *       반환 직전 {@link TestProcessorAdapter#createNewInstance()}를 호출해
 *       테스트 스코프의 상태(예: 내부 모의 자원)로 바인딩한다.</li>
 *   <li><b>정리:</b> 각 테스트 종료 시({@code afterEach})
 *        모든 {@code TestProcessorAdapter}에 대해 {@code close()}를 호출해
 *        테스트 스코프 상태를 정리한다(예: ThreadLocal 제거, 모의 자원 종료 등).</li>
 * </ul>
 *
 * <h3>스레드 모델 / 제약</h3>
 *
 * <p><b>아주 중요:</b> 이 확장은 <em>파라미터 주입(resolveParameter) → 테스트 본문 실행 → afterEach 정리</em>가
 * <b>동일한 스레드</b>에서 수행된다는 전제하에 동작한다.
 * 테스트 어댑터 구현체는 내부적으로 스레드에 귀속된 상태(예: ThreadLocal)를 가질 수 있다.</p>
 *
 * <p><b>따라서 다음은 금지:</b>
 * <ul>
 *   <li><b>{@code @Timeout(threadMode = SEPARATE_THREAD)}</b>
 *        또는 유사한 <b>공유/별도 스레드 실행(shared/separate thread)</b> 모드</li>
 *   <li>테스트 본문에서 보조 스레드를 띄운 뒤 그 보조 스레드에서 어댑터 API를 직접 호출하는 행위</li>
 * </ul>
 * 반드시 <b>{@code SAME_THREAD}</b>에서 실행해야 하며, 병렬 실행을 사용하더라도
 * 각 테스트 케이스는 자기 스레드 내에서만 어댑터를 사용해야 한다.</p>
 *
 * <h3>사용 예</h3>
 * <pre>{@code
 * @ExtendWith(RuntimeBootstrapExtension.class)
 * class MyTest {
 *   @Test
 *   void works(@Proc(TestKafkaLogCollectorAdapter.class) TestKafkaLogCollectorAdapter adapter) {
 *     adapter.createNewInstance(); // 보통은 확장에서 호출해줌
 *     // ... 테스트 본문 ...
 *   }
 * }
 * }</pre>
 *
 * <p>오류 또는 정리 실패는 테스트 안정성을 해치지 않도록 내부에서 안전하게 처리한다(필요 시 로깅/서프레스트).</p>
 */
public class RuntimeBootstrapExtension
                      implements BeforeAllCallback, ParameterResolver, AfterEachCallback {

  private static final AtomicBoolean STARTED = new AtomicBoolean();
  private static final AtomicInteger COUNT = new AtomicInteger(0);
  private static final
      FutureTask<Map<Class<? extends LogProcessorPort>, LogProcessorPort>> TASK
        = new FutureTask<>(() -> {
          COUNT.incrementAndGet();
          return SettingLoader.loadRuntimeSettingFromClasspath().createProcessorChain()
              .stream().collect(Collectors.toMap(
                  a -> a.getClass(),
                  Function.identity(),
                  (a, b) -> a,
                  LinkedHashMap::new
              ));
        });

  private int initCount() {
    return COUNT.get();
  }

  private static void ensureStarted() {
    if (STARTED.compareAndSet(false, true)) {
      TASK.run();
    }
    try {
      TASK.get();
    } catch (Exception e) {
      // Fail-Fast
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws IllegalStateException {
    ensureStarted();
    // 확장 내부에선 AssertJ 대신 예외를 던지는 편이 안전
    if (initCount() != 1) {
      throw new IllegalStateException("Init should run exactly once per JVM, count=" + COUNT.get());
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
    return pc.isAnnotated(Proc.class)
                && TestProcessorAdapter.class.isAssignableFrom(pc.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
    Class<?> type = pc.findAnnotation(Proc.class).orElseThrow().value();

    TestProcessorAdapter p = null;
    try {
      p = (TestProcessorAdapter) TASK.get().get(type);
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException("파라미터 세팅 도중 문제가 발생하였습니다.", e);
    }

    if (!(p instanceof TestProcessorAdapter a)) {
      //noinspection ConstantValue
      throw new ParameterResolutionException(
          "Adapter must implement TestProcessorAdapter: " + (p == null ? "null" : p.getClass()));
    }

    a.createNewInstance();
    return a;
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    TASK.get().values().forEach(this::closeResource);
  }

  private void closeResource(LogProcessorPort processor) {
    if (processor instanceof AutoCloseable) {
      try {
        ((AutoCloseable) processor).close();
      } catch (Exception ignore) {
        //한 두개의 resource가 제대로 처리 안되더라도 크게 문제는 없음
      }
    }
  }
}
