package com.rihee.alerting.loggingService.core.pipeline.port.out;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;

public abstract class LogPersistencePort implements LogProcessorPort {

  private static final String STAGE = "Persistence";

  @Override
  public final String stage() {
    return STAGE;
  }

  @Override
  public abstract ProcessResult process(LogProcessingContext processingContext);
}
