package com.rihee.alerting.loggingService.core.message;

import java.util.HashMap;
import java.util.Map;

public class LogErrorMessage implements LogMessage {

  private final Map<String, Object> allLogs;

  public LogErrorMessage(Map<String, Object> allLogs) {
    this();
    this.allLogs.putAll(allLogs);
  }

  private LogErrorMessage() {
    this.allLogs = new HashMap<>();
  }

  @Override
  public boolean isError() {
    return true;
  }

  @Override
  public Object get(String key) {
    return this.allLogs.get(key);
  }

  @Override
  public void put(String key, Object value) {
    this.allLogs.put(key, value);
  }

  @Override
  public Map<String, Object> toPersistenceMap() {
    return new HashMap<>(this.allLogs);
  }
}
