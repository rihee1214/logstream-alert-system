package com.rihee.alerting.loggingService.core;

public interface LogProcessorSpec {

  LogProcessor newProcessorInstance();
  String getType();
}
