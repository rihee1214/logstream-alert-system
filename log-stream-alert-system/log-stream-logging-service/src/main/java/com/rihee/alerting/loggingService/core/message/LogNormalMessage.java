package com.rihee.alerting.loggingService.core.message;

import com.rihee.alerting.common.constant.message.LogFieldKey;
import com.rihee.alerting.common.util.MapUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LogNormalMessage implements LogMessage {

  private static final Set<String> STRUCTURED_KEYS;

  private final String messageKey;
  private final Map<String, Object> structuredLogs = new HashMap<>();
  private final Map<String, Object> unstructuredLogs = new HashMap<>();

  static {
    try (ScanResult scanResult = new ClassGraph()
        .enableClassInfo()
        .acceptPackages("com.rihee.alerting.common.log.fields") // 💡 특정 패키지만 스캔
        .scan()) {

      STRUCTURED_KEYS = scanResult.getClassesImplementing(LogFieldKey.class.getName())
          .filter(ClassInfo::isEnum)
          .stream()
          .map(ClassInfo::loadClass)
          .filter(LogFieldKey.class::isAssignableFrom)
          .flatMap(c -> {
            @SuppressWarnings("unchecked")
            Class<? extends LogFieldKey> typedClass = (Class<? extends LogFieldKey>) c;
            return Arrays.stream(typedClass.getEnumConstants());
          })
          .map(LogFieldKey::getFieldName)
          .collect(Collectors.toUnmodifiableSet());
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
    if (STRUCTURED_KEYS.contains(key)) {
      return structuredLogs.get(key);
    } else {
      return unstructuredLogs.get(key);
    }
  }

  @Override
  public void put(String key, Object value) {
    if (STRUCTURED_KEYS.contains(key)) {
      structuredLogs.put(key, value);
    } else {
      unstructuredLogs.put(key, value);
    }
  }

  @Override
  public Map<String, Object> toPersistenceMap() {
    Map<String, Object> result = new HashMap<>(this.structuredLogs);
    result.put("meta", MapUtils.toJsonString(this.unstructuredLogs));
    return result;
  }

  @Override
  public String getMessageKey() {
    return this.messageKey;
  }
}
