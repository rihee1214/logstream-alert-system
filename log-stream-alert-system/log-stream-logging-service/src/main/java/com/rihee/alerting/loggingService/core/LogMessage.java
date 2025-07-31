package com.rihee.alerting.loggingService.core;

import com.jsoniter.output.JsonStream;
import com.rihee.alerting.common.constant.log.LogFieldKey;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LogMessage {

  private static final Set<String> STRUCTURED_KEYS;

  private Map<String, Object> structuredLogs = new HashMap<>();
  private Map<String, Object> unstructuredLogs = new HashMap<>();

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

  public LogMessage(Map<String, Object> allLogs) {
    this();
    for (Map.Entry<String, Object> entry : allLogs.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (STRUCTURED_KEYS.contains(key)) {
        this.structuredLogs.put(key, value);
      } else {
        this.unstructuredLogs.put(key, value);
      }
    }
  }

  private LogMessage() {

  }

  public static LogMessage emptyMessage() {
    return new LogMessage();
  }

  public Object get(LogFieldKey key) {
    return structuredLogs.get(key.getFieldName());
  }

  public Object get(String key) {
    if (STRUCTURED_KEYS.contains(key)) {
      return structuredLogs.get(key);
    } else {
      return unstructuredLogs.get(key);
    }
  }

  public void put(String key, Object value) {
    if (STRUCTURED_KEYS.contains(key)) {
      structuredLogs.put(key, value);
    } else {
      unstructuredLogs.put(key, value);
    }
  }

  public Map<String, Object> toPersistenceMap() {
    Map<String, Object> result = new HashMap<>(structuredLogs);
    result.put("meta", JsonStream.serialize(unstructuredLogs));
    return result;
  }
}
