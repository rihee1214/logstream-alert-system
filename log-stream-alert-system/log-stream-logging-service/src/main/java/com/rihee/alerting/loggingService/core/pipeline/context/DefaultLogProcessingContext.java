package com.rihee.alerting.loggingService.core.pipeline.context;

import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import java.util.ArrayList;
import java.util.List;

public class DefaultLogProcessingContext implements LogProcessingContext {

  private final List<LogMessage> logMessages = new ArrayList<>();

  @Override
  public void stackingLogMessage(LogMessage message) {
    this.logMessages.add(message);
  }
}
