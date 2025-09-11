package com.rihee.alerting.loggingService.core.model;

import com.rihee.alerting.common.constant.message.LogFieldKey;
import com.rihee.alerting.common.constant.storage.NormalLogSchema;
import com.rihee.alerting.common.util.MapUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LogNormalMessage implements LogMessage {


  private static final int LOG_VERSION_MAJOR = 1;

  private final String messageKey;
  private final Map<StructuredRouter, Map<String, Object>> logMap
                                  = Map.of(
                                      StructuredRouter.NORMAL, new HashMap<>(),
                                      StructuredRouter.CALL, new HashMap<>(),
                                      StructuredRouter.NONE, new HashMap<>()
                                    );

  private enum StructuredRouter {
    NORMAL,
    CALL,
    NONE;

    private static final Set<String> STRUCTURED_KEYS;
    private static final Set<String> STRUCTURED_CALL_KEYS;
    private static final String CALL_PREFIX = "call.";

    static {
      Set<String> normal;
      Set<String> call;

      try (ScanResult sr = new ClassGraph()
          .enableClassInfo()
          .acceptPackages(LogFieldKey.class.getPackageName()) // 하위 패키지까지 포함됨
          .scan()) {

        // 1) 모든 enum 상수의 fieldName 스트림
        Stream<String> names = sr.getClassesImplementing(LogFieldKey.class.getName())
            .filter(ClassInfo::isEnum)
            .stream()
            .map(ci -> (Class<? extends LogFieldKey>) ci.loadClass(LogFieldKey.class))
            .flatMap(c -> Arrays.stream(c.getEnumConstants()))
            .map(LogFieldKey::getFieldName);

        // 2) call.* 여부로 한 번에 분할
        var parts = names.collect(Collectors.partitioningBy(
            n -> n.startsWith(CALL_PREFIX),
            Collectors.toCollection(LinkedHashSet::new)
        ));

        call = parts.get(true);
        normal = parts.get(false);
      }

      STRUCTURED_CALL_KEYS = Collections.unmodifiableSet(call);
      STRUCTURED_KEYS      = Collections.unmodifiableSet(normal);
    }

    public static StructuredRouter routeKey(String key) {
      if (STRUCTURED_KEYS.contains(key)) {
        return NORMAL;
      } else if (STRUCTURED_CALL_KEYS.contains(key)) {
        return CALL;
      } else {
        return NONE;
      }
    }
  }

  /**
   * 주어진 로그 맵을 structured / unstructured 로 분리하여 초기화합니다.
   * 분리 기준은 {@link LogFieldKey}를 기반으로 합니다.
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
    return new LogNormalMessage(allLogs, messageKey);
  }

  @Override
  public boolean isError() {
    return false;
  }

  @Override
  public Object get(String key) {
    if ("call".equals(key)) {
      return MapUtils.toJsonString(logMap.get(StructuredRouter.CALL));
    } else if ("meta".equals(key)) {
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
    result.put("meta", logMap.get(StructuredRouter.NONE));
    result.put("call", logMap.get(StructuredRouter.CALL));
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
