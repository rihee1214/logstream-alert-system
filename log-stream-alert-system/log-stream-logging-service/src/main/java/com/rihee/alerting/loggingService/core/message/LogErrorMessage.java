package com.rihee.alerting.loggingService.core.message;

import com.rihee.alerting.common.constant.storage.ErrorLogSchema;
import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

public class LogErrorMessage implements LogMessage {

  private static final int LOG_MAJOR_VERSION = 1;

  private final Map<String, Object> errorLogs;

  private LogErrorMessage(Map<String, Object> errorLogs) {
    this.errorLogs = new HashMap<>(errorLogs);
  }

  public static LogErrorMessage fromOriginMessage(String originLog,
                                                          String messageKey,
                                                          String reason) {
    Map<String, Object> errorLogs = buildErrorLogs(originLog, messageKey, reason);
    return new LogErrorMessage(errorLogs);
  }

  public static LogErrorMessage fromNormalMessage(LogMessage message, String reason) {
    String messageKey = message.getMessageKey();
    String originLog = MapUtils.toJsonString(message.toPersistenceMap());
    Map<String, Object> errorLogs = buildErrorLogs(originLog, messageKey, reason);
    return new LogErrorMessage(errorLogs);
  }

  private static Map<String, Object> buildErrorLogs(String originLog,
                                                            String messageKey,
                                                            String reason) {
    if (StringUtils.isBlank(messageKey)) {
      throw new IllegalArgumentException("messageKey가 제대로 존재하지 않습니다.");
    }

    if (StringUtils.isBlank(reason)) {
      throw new IllegalArgumentException("에러의 이유가 기재되어있지 않습니다.");
    }

    Map<String, Object> errorLogs = new HashMap<>();
    errorLogs.put(ErrorLogSchema.MESSAGE_ID.getSchemaName(), messageKey);
    errorLogs.put(ErrorLogSchema.ORIGIN_LOG.getSchemaName(), originLog);
    errorLogs.put(ErrorLogSchema.REASON.getSchemaName(), reason);
    errorLogs.put(ErrorLogSchema.LOG_VERSION_MAJOR.getSchemaName(), LOG_MAJOR_VERSION);
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
    return this.errorLogs.get(ErrorLogSchema.MESSAGE_ID.getSchemaName()).toString();
  }
}
