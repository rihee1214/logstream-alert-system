package com.rihee.alerting.loggingService.core.message;

import java.util.HashMap;
import java.util.Map;

public class LogErrorMessage implements LogMessage {

  private final Map<String, Object> allLogs;
  private final String messageKey;

  public LogErrorMessage(Map<String, Object> allLogs, String messageKey) {
    this(messageKey);
    this.allLogs.putAll(allLogs);
  }

  private LogErrorMessage(String messageKey) {
    this.allLogs = new HashMap<>();
    this.messageKey = messageKey;
  }

  public static LogErrorMessage fromNormalMessage(LogMessage message) {
    return new LogErrorMessage(message.toPersistenceMap(), message.getMessageKey());
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

  @Override
  public String getMessageKey() {
    return messageKey;
  }
}
