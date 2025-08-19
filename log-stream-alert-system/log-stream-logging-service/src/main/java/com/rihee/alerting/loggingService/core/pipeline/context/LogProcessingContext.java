package com.rihee.alerting.loggingService.core.pipeline.context;

import com.rihee.alerting.loggingService.core.model.LogMessage;
import java.util.Iterator;

public interface LogProcessingContext {

  void stackingLogMessage(LogMessage message);

  Iterator<LogMessage> iterator();

  boolean isEmpty();
}
