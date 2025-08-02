package com.rihee.alerting.loggingService.core.pipeline;

public interface CommitableLogProcessor extends  LogProcessor {

  void commit();
}
