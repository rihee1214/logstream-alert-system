package com.rihee.alerting.loggingService.core.model;

import java.util.Map;

public interface LogMessage {

  String getMessageKey();

  boolean isError();

  Object get(String key);

  void put(String key, Object value);

  Map<String, Object> toPersistenceMap();
}
