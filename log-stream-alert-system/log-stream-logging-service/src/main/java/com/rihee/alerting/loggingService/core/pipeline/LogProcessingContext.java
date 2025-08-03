package com.rihee.alerting.loggingService.core.pipeline;

import com.rihee.alerting.loggingService.core.message.LogMessage;

public interface LogProcessingContext {

  void stackingLogMessage(LogMessage message);
}
