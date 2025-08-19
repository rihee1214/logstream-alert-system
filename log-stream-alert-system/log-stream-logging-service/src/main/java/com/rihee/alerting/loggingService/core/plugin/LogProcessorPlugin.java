package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessor;

public interface LogProcessorPlugin {

  LogProcessor newProcessorInstance();
  String getProcessorType();
}
