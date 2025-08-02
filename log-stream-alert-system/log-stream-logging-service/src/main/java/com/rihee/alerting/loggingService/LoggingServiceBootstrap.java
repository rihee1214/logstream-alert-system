package com.rihee.alerting.loggingService;

import com.rihee.alerting.loggingService.core.runtime.LogWorker;
import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoggingServiceBootstrap {

  public static void main(String[] args) {

    LoggingRuntimeConfig config = SettingLoader.loadRuntimeSettingFromClasspath();

    int threadCount = config.getWorkerThreadCount();
    ExecutorService service = Executors.newFixedThreadPool(threadCount);
    for(int i = 0; i < threadCount; i++) {
      service.execute(new LogWorker(config));
    }
  }

}
