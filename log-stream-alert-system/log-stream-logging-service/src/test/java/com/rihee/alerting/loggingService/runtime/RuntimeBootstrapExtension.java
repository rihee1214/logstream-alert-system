package com.rihee.alerting.loggingService.runtime;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import com.rihee.alerting.loggingService.testinfra.common.TestParameter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 확장: 런타임 파이프라인을 한 번만 초기화하고, 각 테스트에 테스트 어댑터를 주입/정리한다.
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
public class RuntimeBootstrapExtension implements BeforeAllCallback, ParameterResolver {

  private static final Namespace NS = Namespace.create(RuntimeBootstrapExtension.class);
  private static final AtomicBoolean STARTED = new AtomicBoolean();
  private static final AtomicInteger COUNT = new AtomicInteger(0);
  private static final
      FutureTask<LoggingRuntimeConfig> TASK
        = new FutureTask<>(() -> {
          COUNT.incrementAndGet();
          return SettingLoader.loadRuntimeSettingFromClasspath();
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
    return pc.isAnnotated(TestParameter.class)
                && LogProcessorPort.class.isAssignableFrom(pc.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
    Class<?> type = pc.findAnnotation(TestParameter.class).orElseThrow().value();

    try {
      for (LogProcessorPort target : TASK.get().createProcessorChain()) {
        if (type.isInstance(target)) {
          ec.getStore(NS).put(pc.getIndex(), target);
          return type.cast(target);
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException("파라미터 세팅 도중 문제가 발생하였습니다.", e);
    }

    throw new IllegalStateException("넣어준 타입에 맞는 클래스의 인스턴스가 존재하지 않습니다. annotation을 다시 확인하세요.");
  }

}
