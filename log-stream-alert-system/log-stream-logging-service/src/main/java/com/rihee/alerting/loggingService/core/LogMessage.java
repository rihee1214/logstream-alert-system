package com.rihee.alerting.loggingService.core;

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

  private Map<String, Object> allLogs;
  private final Set<String> structuredKeys;

  public LogMessage(Map<String, Object> allLogs) {
    this();
    this.allLogs = new HashMap<>(allLogs);
  }

  private LogMessage() {
    try (ScanResult scanResult = new ClassGraph()
        .enableClassInfo()
        .acceptPackages("com.rihee.alerting.common.log.fields") // 💡 특정 패키지만 스캔
        .scan()) {

      this.structuredKeys = scanResult.getClassesImplementing(LogFieldKey.class.getName())
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

  public static LogMessage getEmptyMessage() {
    return new LogMessage();
  }

  public Object get(LogFieldKey fieldKey) {
    return allLogs.get(fieldKey.getFieldName());
  }

  public void put() {

  }
}
