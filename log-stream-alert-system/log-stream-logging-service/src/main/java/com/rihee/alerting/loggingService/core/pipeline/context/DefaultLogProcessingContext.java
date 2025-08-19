package com.rihee.alerting.loggingService.core.pipeline.context;

import com.rihee.alerting.loggingService.core.model.LogMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultLogProcessingContext implements LogProcessingContext {

  private final List<LogMessage> logMessages = new ArrayList<>();

  @Override
  public void stackingLogMessage(LogMessage message) {
    this.logMessages.add(message);
  }

  @Override
  public Iterator<LogMessage> iterator() {
    return logMessages.iterator();
  }

  @Override
  public boolean isEmpty() {
    return logMessages.isEmpty();
  }
}
