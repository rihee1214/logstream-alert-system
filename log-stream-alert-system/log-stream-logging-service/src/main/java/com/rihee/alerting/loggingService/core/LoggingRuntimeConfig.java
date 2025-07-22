package com.rihee.alerting.loggingService.core;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.rihee.alerting.loggingService.validators.LogValidator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class LoggingRuntimeConfig {

  private LogCollector logCollector;
  private LogValidator logValidator;
  private LogPersistence logPersistence;

  private LoggingRuntimeConfig(LogCollector logCollector,
                                LogValidator logValidator, LogPersistence logPersistence) {
    this.logCollector = logCollector;
    this.logValidator = logValidator;
    this.logPersistence = logPersistence;
  }

  public static Builder getBuilder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  public static Class<? extends LogCollector> resolveCollector(String collectorMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages("com.rihee.alerting.loggingService.collectors") // 스캔 범위 제한
        .scan()) {

      return scanResult.getClassesWithAnnotation(CollectorType.class.getName())
          .stream()
          .map(ci -> {
            try {
              return (Class<?>) Class.forName(ci.getName());
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .filter(clazz -> {
            CollectorType annotation = clazz.getAnnotation(CollectorType.class);
            return annotation != null && annotation.value().equals(collectorMode);
          })
          .map(clazz -> (Class<? extends LogCollector>) clazz)
          .findFirst()
          .orElseThrow(()
                -> new IllegalStateException("No collector found for target: " + collectorMode));
    }
  }

  @SuppressWarnings("unchecked")
  public static Class<? extends LogPersistence> resolvePersistence(String persistenceMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages("com.rihee.alerting.loggingService.persistence") // 스캔 범위 제한
        .scan()) {

      return scanResult.getClassesWithAnnotation(PersistenceType.class.getName())
          .stream()
          .map(ci -> {
            try {
              return (Class<?>) Class.forName(ci.getName());
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .filter(clazz -> {
            PersistenceType annotation = clazz.getAnnotation(PersistenceType.class);
            return annotation != null && annotation.value().equals(persistenceMode);
          })
          .map(clazz -> (Class<? extends LogPersistence>) clazz)
          .findFirst()
          .orElseThrow(()
              -> new IllegalStateException("No collector found for target: " + persistenceMode));
    }
  }

  protected static class Builder {

    private LogCollector logCollector;
    private LogValidator logValidator;
    private LogPersistence logPersistence;

    public LoggingRuntimeConfig build() {
      return new LoggingRuntimeConfig(this.logCollector, this.logValidator, this.logPersistence);
    }
  }
}
