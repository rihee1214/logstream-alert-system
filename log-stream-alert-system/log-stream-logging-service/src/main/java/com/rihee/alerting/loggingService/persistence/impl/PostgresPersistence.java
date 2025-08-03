package com.rihee.alerting.loggingService.persistence.impl;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import java.util.List;
import java.util.Map;

@PersistenceType("postgres")
public final class PostgresPersistence extends LogPersistence {

  private PostgresPersistence() {

  }

  @Override
  public List<LogMessage> process(List<LogMessage> messages) {
    return List.of();
  }

  public static LogProcessor.Builder<?> builder() {
    return new Builder();
  }

  public static class Builder implements LogProcessor.Builder<PostgresPersistence> {


    @Override
    public LogProcessor.Builder<PostgresPersistence>
                                            withProperties(Map<String, String> setting) {
      return this;
    }

    @Override
    public PostgresPersistence build() {
      return new PostgresPersistence();
    }
  }
}
