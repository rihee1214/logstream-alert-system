package com.rihee.alerting.loggingService.core.message;

import com.rihee.alerting.common.util.MapUtils;
import java.util.HashMap;
import java.util.Map;

public class LogErrorMessage implements LogMessage {

  private final String messageKey;
  private final Map<String, Object> errorLogs;

  private LogErrorMessage(Map<String, Object> errorLogs, String messageKey) {
    this.errorLogs = new HashMap<>(errorLogs);
    this.messageKey = messageKey;
  }

  public static LogErrorMessage fromOriginMessage(String originLog, String messageKey) {
    Map<String, Object> errorLogs = buildErrorLogs(originLog, messageKey);
    return new LogErrorMessage(errorLogs, messageKey);
  }

  public static LogErrorMessage fromNormalMessage(LogMessage message) {
    String messageKey = message.getMessageKey();
    String originLog = MapUtils.toJsonString(message.toPersistenceMap());
    Map<String, Object> errorLogs = buildErrorLogs(originLog, messageKey);
    return new LogErrorMessage(errorLogs, messageKey);
  }

  private static Map<String, Object> buildErrorLogs(String originLog, String messageKey) {
    Map<String, Object> errorLogs = new HashMap<>();
    errorLogs.put("messageKey", messageKey);
    errorLogs.put("originLog", originLog);
    return errorLogs;
  }

  @Override
  public boolean isError() {
    return true;
  }

  @Override
  public Object get(String key) {
    return this.errorLogs.get(key);
  }

  @Override
  public void put(String key, Object value) {
    this.errorLogs.put(key, value);
  }

  @Override
  public Map<String, Object> toPersistenceMap() {
    return new HashMap<>(this.errorLogs);
  }

  @Override
  public String getMessageKey() {
    return messageKey;
  }
}
