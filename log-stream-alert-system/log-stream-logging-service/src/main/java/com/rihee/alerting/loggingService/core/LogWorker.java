package com.rihee.alerting.loggingService.core;

import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.rihee.alerting.loggingService.validators.LogValidator;
import java.util.List;

public class LogWorker implements Runnable {

  private final List<? extends LogProcessor> logProcessors;

  public LogWorker(LoggingRuntimeConfig config) {
    this.logProcessors = config.createProcessorChain();
  }

  @Override
  public void run() {

  }
}
