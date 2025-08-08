package com.rihee.alerting.loggingService.core.message;

import com.rihee.alerting.common.constant.storage.ErrorLogSchema;
import com.rihee.alerting.common.util.MapUtils;
import java.util.HashMap;
import java.util.Map;

// TODO 전체적으로 구조 변경이 필요함.
//   1. 내부적으로 Map 하나만 가지고 관리하기.
//   2. set 메서드는 살리되 타입 검증이 제대로 되어야함. (messageKey와 같이 중요필드에 대한 필수값 검증 같은것)
//   3. reason field가 늘어남에 따라 그것을 주입받는 생성자 필요함 (모든 생성 위치에 추가하도록 해야함)
public class LogErrorMessage implements LogMessage {

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
    if (messageKey == null || messageKey.isBlank()) {
      throw new IllegalArgumentException("messageKey가 제대로 존재하지 않습니다.");
    }

    Map<String, Object> errorLogs = new HashMap<>();
    errorLogs.put(ErrorLogSchema.MESSAGE_ID.getSchemaName(), messageKey);
    errorLogs.put(ErrorLogSchema.ORIGIN_LOG.getSchemaName(), originLog);
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
