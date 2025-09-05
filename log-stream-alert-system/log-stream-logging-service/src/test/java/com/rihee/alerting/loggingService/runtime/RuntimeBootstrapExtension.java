package com.rihee.alerting.loggingService.runtime;

import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class RuntimeBootstrapExtension implements BeforeAllCallback, ParameterResolver {

  private static final AtomicBoolean STARTED = new AtomicBoolean();
  private static final AtomicInteger COUNT = new AtomicInteger(0);
  private static final FutureTask<LoggingRuntimeConfig> TASK
        = new FutureTask<>(() -> {
          COUNT.incrementAndGet();
          return SettingLoader.loadRuntimeSettingFromClasspath();
        });

  private static void ensureStarted() {
    if (STARTED.compareAndSet(false, true)) {
      TASK.run();
    }
  }

  private int initCount() {
    return COUNT.get();
  }

  private LoggingRuntimeConfig config() {
    ensureStarted();
    try {
      return TASK.get();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    ensureStarted();
    // 확장 내부에선 AssertJ 대신 예외를 던지는 편이 안전
    if (initCount() != 1) {
      throw new IllegalStateException("Init should run exactly once per JVM, count=" + COUNT.get());
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == LoggingRuntimeConfig.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return config().createProcessorChain();
  }
}
