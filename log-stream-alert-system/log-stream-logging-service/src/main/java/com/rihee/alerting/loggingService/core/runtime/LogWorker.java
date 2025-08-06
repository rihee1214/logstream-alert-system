package com.rihee.alerting.loggingService.core.runtime;

import com.rihee.alerting.loggingService.core.pipeline.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWorker implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(LogWorker.class);
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
    LogProcessingContext context = new DefaultLogProcessingContext();

    boolean commitPermitted = true;

    for (LogProcessor processor : logProcessors) {
      ProcessResult result = processor.process(context);
      if (!result.shouldContinue()) {
        commitPermitted = result.shouldCommit();
        break;
      }
      context = result.context();
    }

    if (commitPermitted) {
      for (CommitableLogProcessor commitTarget : commitableLogProcessors) {
        try {
          commitTarget.commit();
        } catch (Exception e) {
          log.warn("커밋 실패: {}", commitTarget.getClass().getSimpleName(), e);
        }
      }
    }
  }
}
