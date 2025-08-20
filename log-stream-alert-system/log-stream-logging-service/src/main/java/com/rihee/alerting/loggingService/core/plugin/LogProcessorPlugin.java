package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;

public interface LogProcessorPlugin {

  LogProcessorPort newProcessorInstance();
  String getProcessorType();
}

