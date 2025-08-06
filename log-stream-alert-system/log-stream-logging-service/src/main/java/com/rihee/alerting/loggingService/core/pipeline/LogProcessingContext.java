package com.rihee.alerting.loggingService.core.pipeline;

import com.rihee.alerting.loggingService.core.message.LogMessage;
import java.util.Iterator;

public interface LogProcessingContext {

  void stackingLogMessage(LogMessage message);

  Iterator<LogMessage> iterator();

  boolean isEmpty();
}
