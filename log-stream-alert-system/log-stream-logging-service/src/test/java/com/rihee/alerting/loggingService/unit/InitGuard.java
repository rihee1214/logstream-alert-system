package com.rihee.alerting.loggingService.unit;

import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class InitGuard implements BeforeAllCallback {

  private static final AtomicBoolean STARTED = new AtomicBoolean();
  private static final AtomicInteger COUNT = new AtomicInteger(0);
  private static final FutureTask<LoggingRuntimeConfig> TASK
                    = new FutureTask<>(() -> {
                      COUNT.incrementAndGet();
                      return SettingLoader.loadRuntimeSettingFromClasspath();
                    });

  public static int initCount() {
    return COUNT.get();
  }

  static LoggingRuntimeConfig config() {
    try {
      return TASK.get();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (STARTED.compareAndSet(false, true)) {
      TASK.run(); // 최초 1회만 실행 (무거운 외부 연결 금지: classpath 기반 메타 초기화만)
    }
  }
}
