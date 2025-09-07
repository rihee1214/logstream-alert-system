package com.rihee.alerting.loggingService.runtime;

import com.rihee.alerting.loggingService.adapter.Proc;
import com.rihee.alerting.loggingService.adapter.TestProcessorAdapter;
import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class RuntimeBootstrapExtension
              implements BeforeAllCallback, ParameterResolver {

  private static final AtomicBoolean STARTED = new AtomicBoolean();
  private static final AtomicInteger COUNT = new AtomicInteger(0);
  private static final FutureTask<LoggingRuntimeConfig> TASK
        = new FutureTask<>(() -> {
          COUNT.incrementAndGet();
          return SettingLoader.loadRuntimeSettingFromClasspath();
        });

  private int initCount() {
    return COUNT.get();
  }

  private static LoggingRuntimeConfig ensureStarted() {
    if (STARTED.compareAndSet(false, true)) {
      TASK.run();
    }
    try {
      return TASK.get();
    } catch (Exception e) {
      // Fail-Fast
      throw new IllegalStateException(e);
    }
  }

  private static ExtensionContext.Store classStore(ExtensionContext ctx) {
    return ctx.getStore(ExtensionContext.Namespace.create("msa-log", "per-class",
        ctx.getRequiredTestClass().getName()));
  }

  private static ExtensionContext.Store testStore(ExtensionContext ctx) {
    return ctx.getStore(ExtensionContext.Namespace.create("msa-log", "per-test",
        ctx.getUniqueId()));
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    ensureStarted();
    // 확장 내부에선 AssertJ 대신 예외를 던지는 편이 안전
    if (initCount() != 1) {
      throw new IllegalStateException("Init should run exactly once per JVM, count=" + COUNT.get());
    }
  }

  // 공유 자원 (클래스 스코프)
  final class SharedHolder implements AutoCloseable {
    final LoggingRuntimeConfig cfg;

    // optional: DataSource ds; ExecutorService pool; ...
    SharedHolder(LoggingRuntimeConfig cfg /*, ... */) {
      this.cfg = cfg;
    }

    @Override
    public void close() {
      // ds.close(); pool.shutdown(); 등
    }
  }

  static final class PerTestHolder implements AutoCloseable {

    final Map<String, TestProcessorAdapter> byId;

    PerTestHolder(Map<String, TestProcessorAdapter> byId) {
      this.byId = byId;
    }

    @Override
    public void close() {
      byId.values().forEach(a -> {
        try {
          a.close();
        } catch (Exception ignore) {
          // ignore
        }
      });
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
    Class<?> t = pc.getParameter().getType();
    if (t == LoggingRuntimeConfig.class) {
      return true;
    }
    return pc.isAnnotated(Proc.class) && TestProcessorAdapter.class.isAssignableFrom(t);
  }

  @Override
  public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
    SharedHolder shared = classStore(ec).getOrComputeIfAbsent(
        "shared", k -> new SharedHolder(ensureStarted()), SharedHolder.class);

    if (pc.getParameter().getType() == LoggingRuntimeConfig.class) {
      return shared.cfg;
    }

    var id = pc.findAnnotation(Proc.class).orElseThrow().value();
    PerTestHolder perTest = testStore(ec).getOrComputeIfAbsent(
        "perTest",
        k -> new PerTestHolder((buildPerTestAdapters(shared.cfg))),
        PerTestHolder.class
    );
    TestProcessorAdapter a = perTest.byId.get(id);
    if (a == null) {
      throw new ParameterResolutionException("No adapter for id=" + id);
    }
    return a;
  }

  // 운영 체인 → 테스트 어댑터 맵
  private static Map<String, TestProcessorAdapter> buildPerTestAdapters(LoggingRuntimeConfig cfg) {
    var chain = cfg.createProcessorChain(); // List<원본 Processor/Adapter>
    var map = new LinkedHashMap<String, TestProcessorAdapter>();
    for (var p : chain) {
      String id = extractId(p);           // 예: p.descriptor().id()
      map.put(id, toTestAdapter(p));      // 예: 새 Mock/Recording로 감싼 TestAdapter
    }
    return map;
  }

  private static String extractId(Object p) {
    if (!(p instanceof TestProcessorAdapter)) {
      throw new IllegalStateException("해당 요소의 타입이 TestProcessorAdapter가 아닙니다.");
    }
    return ((TestProcessorAdapter) p).id();
  }

  private static TestProcessorAdapter toTestAdapter(Object p) {
    return ((TestProcessorAdapter) p);
  }
}
