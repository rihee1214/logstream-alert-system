package com.rihee.alerting.loggingService.testinfra.common;

public interface TestProcessorAdapter extends AutoCloseable {

  void createNewInstance();
}
