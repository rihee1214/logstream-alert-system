package com.rihee.alerting.loggingService.persistence.impl;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.LogMessage;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import java.util.List;
import java.util.Map;

@PersistenceType("postgres")
public final class PostgresPersistence extends LogPersistence {

  public static LogPersistence.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public List<LogMessage> process(List<LogMessage> messages) {
    return List.of();
  }

  protected static class Builder
                      implements LogPersistence.Builder<PostgresPersistence> {

    @Override
    public LogPersistence.Builder<PostgresPersistence>
                                            withProperties(Map<String, String> setting) {
      return null;
    }

    @Override
    public PostgresPersistence build() {
      return null;
    }
  }
}
