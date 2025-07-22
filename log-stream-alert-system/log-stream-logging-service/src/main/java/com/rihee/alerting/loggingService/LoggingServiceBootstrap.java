package com.rihee.alerting.loggingService;

import com.rihee.alerting.loggingService.core.LogWorker;
import com.rihee.alerting.loggingService.core.SettingLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoggingServiceBootstrap {

  public static void main(String[] args) {

    SettingLoader loader = new SettingLoader();


    int threadCount = 8;
    ExecutorService service = Executors.newFixedThreadPool(threadCount);
    for(int i = 0; i < threadCount; i++) {
      service.execute(new LogWorker());
    }
  }

}
