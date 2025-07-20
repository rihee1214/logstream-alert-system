package com.rihee.alerting.loggingService;

import com.rihee.alerting.loggingService.core.LoggingWorker;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoggingServiceApplication {

  public static void main(String[] args) {
    int threadCount = 8;
    ExecutorService service = Executors.newFixedThreadPool(threadCount);
    for(int i = 0; i < threadCount; i++) {
      service.execute(new LoggingWorker());
    }
  }

}
