package com.rihee.alerting.loggingService.persistence.impl;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import java.util.Map;

@PersistenceType("postgresql")
public final class PostgreSqlPersistence extends LogPersistence {

  public static LogPersistence.Builder<?> builder() {
    return new Builder();
  }

  protected static class Builder implements LogPersistence.Builder<PostgreSqlPersistence> {

    @Override
    public LogPersistence.Builder<PostgreSqlPersistence>
                                            withProperties(Map<String, String> setting) {
      return null;
    }

    @Override
    public PostgreSqlPersistence build() {
      return null;
    }
  }
}
