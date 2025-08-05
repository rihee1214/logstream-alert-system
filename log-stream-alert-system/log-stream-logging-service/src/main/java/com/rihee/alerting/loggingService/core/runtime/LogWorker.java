package com.rihee.alerting.loggingService.core.runtime;

import com.rihee.alerting.loggingService.core.pipeline.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import java.util.List;
import java.util.stream.Collectors;

public class LogWorker implements Runnable {

  private final List<? extends LogProcessor> logProcessors;
  private final List<? extends CommitableLogProcessor> commitableLogProcessors;

  public LogWorker(LoggingRuntimeConfig config) {
    this.logProcessors = config.createProcessorChain();
    this.commitableLogProcessors = logProcessors.stream()
                                            .filter(CommitableLogProcessor.class::isInstance)
                                            .map(CommitableLogProcessor.class::cast)
                                            .collect(Collectors.toList());
  }

  @Override
  public void run() {
    // TODO 기본 로직은 완성이지만, 로깅처리와 에러 핸들링에 대한 문제를 추가적으로 처리해야한다.
    LogProcessingContext messages = new DefaultLogProcessingContext();
    for (LogProcessor processor : logProcessors) {
      messages = processor.process(messages);
    }

    for (CommitableLogProcessor commitTarget : commitableLogProcessors) {
      commitTarget.commit();
    }
  }
}
