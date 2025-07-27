package com.rihee.alerting.loggingService.core;

import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.rihee.alerting.loggingService.validators.LogValidator;

public class LogWorker implements Runnable {

  private final LogCollector collector;
  private final LogValidator validator;
  private final LogPersistence persistence;

  public LogWorker(LoggingRuntimeConfig config) {
    this.collector = config.getCollectorInstance();
    this.validator = config.getValidatorInstance();
    this.persistence = config.getPersistenceInstance();
  }

  @Override
  public void run() {

  }
}
