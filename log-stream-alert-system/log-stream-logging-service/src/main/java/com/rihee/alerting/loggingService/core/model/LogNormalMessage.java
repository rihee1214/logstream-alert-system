package com.rihee.alerting.loggingService.core.model;

import static com.rihee.alerting.common.constant.storage.NormalLogSchema.META;

import com.rihee.alerting.common.constant.logging.StructuredLogFields;
import com.rihee.alerting.common.constant.storage.NormalLogSchema;
import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LogNormalMessage extends LogMessage {


  private static final int LOG_VERSION_MAJOR = 1;

  private final String messageKey;
  private final Map<StructuredRouter, Map<String, Object>> logMap
                                  = Map.of(
                                      StructuredRouter.NORMAL, new HashMap<>(),
                                      StructuredRouter.NONE, new HashMap<>()
                                    );

  private enum StructuredRouter {
    NORMAL,
    NONE;

    private static final Set<String> STRUCTURED_KEYS = EnumSet.allOf(StructuredLogFields.class)
                                                            .stream()
                                                            .map(StructuredLogFields::getFieldName)
                                                            .collect(Collectors.toUnmodifiableSet());

    public static StructuredRouter routeKey(String key) {
      if (STRUCTURED_KEYS.contains(key)) {
        return NORMAL;
      }  else {
        return NONE;
      }
    }
  }

  /**
   * 주어진 로그 맵을 structured / unstructured 로 분리하여 초기화합니다.
   * 분리 기준은 {@link StructuredLogFields}를 기반으로 합니다.
   * 내부 분류 로직은 put(String, Object)에 위임됩니다.
   */
  private LogNormalMessage(Map<String, Object> allLogs, String messageKey) {
    this.messageKey = messageKey;
    for (Map.Entry<String, Object> entry : allLogs.entrySet()) {
      this.put(entry.getKey(), entry.getValue());
    }
    this.put(NormalLogSchema.LOG_VERSION_MAJOR.getSchemaName(), LOG_VERSION_MAJOR);
  }

  public static LogNormalMessage fromOriginMessage(Map<String, Object> allLogs, String messageKey) {
    String tobeMessageKey = messageKey;
    if (StringUtils.isBlank(tobeMessageKey)) {
      tobeMessageKey = generateKey(allLogs);
    }
    return new LogNormalMessage(allLogs, tobeMessageKey);
  }

  @Override
  public boolean isError() {
    return false;
  }

  @Override
  public Object get(String key) {
    if ("meta".equals(key)) {
      return MapUtils.toJsonString(logMap.get(StructuredRouter.NONE));
    } else {
      return logMap.get(StructuredRouter.routeKey(key)).get(key);
    }
  }

  @Override
  public void put(String key, Object value) {
    logMap.get(StructuredRouter.routeKey(key)).put(key, value);
  }

  @Override
  public String toJsonString() {
    Map<String, Object> result = new HashMap<>(logMap.get(StructuredRouter.NORMAL));
    result.put(META.getSchemaName(), logMap.get(StructuredRouter.NONE));
    return MapUtils.toJsonString(result);
  }

  @Override
  public String getMessageKey() {
    return this.messageKey;
  }

  @Override
  public String toString() {
    return this.toJsonString();
  }
}
