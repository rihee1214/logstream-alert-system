package com.rihee.alerting.loggingService.core.pipeline;

public interface LogProcessorSpec {

  LogProcessor newProcessorInstance();
  String getProcessorType();
}
