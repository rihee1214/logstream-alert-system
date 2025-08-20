package com.rihee.alerting.loggingService.core.runtime;

import com.rihee.alerting.loggingService.core.pipeline.api.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWorker implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(LogWorker.class);
  private final List<? extends LogProcessorPort> logProcessors;
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
    // TODO 동작 처리 및 로그 처리 관련 고민 필요
    try {
      while (true) {
        try {
          process();
        } catch (RuntimeException e) {
          // TODO 실패한 건들에 대해서 어떻게 처리할지 고민할 것
          log.warn("", e);
        }
      }
    } catch (Exception e) {

      log.error("", e);
    }
  }

  private void process() {
    LogProcessingContext context = new DefaultLogProcessingContext();

    boolean commitPermitted = true;

    for (LogProcessorPort processor : logProcessors) {
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
