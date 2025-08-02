package com.rihee.alerting.loggingService.core.message;

import java.util.Map;

public interface LogMessage {

  boolean isError();

  Object get(String key);

  void put(String key, Object value);

  Map<String, Object> toPersistenceMap();
}
