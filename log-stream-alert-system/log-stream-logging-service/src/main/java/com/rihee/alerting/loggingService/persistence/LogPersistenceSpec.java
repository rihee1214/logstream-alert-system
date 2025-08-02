package com.rihee.alerting.loggingService.persistence;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessorSpec;
import com.rihee.alerting.loggingService.persistence.LogPersistence.Builder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class LogPersistenceSpec implements LogProcessorSpec {

  private final Builder<?> builder;
  private final String persistenceType;

  public LogPersistenceSpec(Map<String, String> setting) {
    this.persistenceType = setting.get("persistence.type");
    if (this.persistenceType == null || this.persistenceType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'persistence.type' 이 존재하지 않습니다.");
    }

    this.builder = resolvePersistenceBuilder(this.persistenceType)
                            .withProperties(setting);
  }

  @SuppressWarnings("unchecked")
  private static LogPersistence.Builder<?> resolvePersistenceBuilder(String persistenceMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages("com.rihee.alerting.loggingService.persistence.impl") // 스캔 범위 제한
        .scan()) {

      // PersistenceType annotation과 일치하는 클래스 찾기
      Class<? extends LogPersistence> persistenceClass = scanResult
          .getClassesWithAnnotation(PersistenceType.class.getName())
          .stream()
          .map(classInfo -> {
            try {
              return (Class<?>) Class.forName(classInfo.getName());
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
          .orElseThrow(() ->
              new IllegalStateException("해당 persistenceMode에 맞는 클래스가 존재하지 않습니다: " + persistenceMode));

      // static builder() 메서드 존재 여부 확인 및 호출
      Method builderMethod = persistenceClass.getDeclaredMethod("builder");
      if (!Modifier.isStatic(builderMethod.getModifiers())) {
        throw new IllegalStateException("builder() 메서드는 static이어야 합니다.");
      }

      return (LogPersistence.Builder<?>) builderMethod.invoke(null);

    } catch (Exception e) {
      throw new RuntimeException("Persistence 빌더 생성 실패: " + persistenceMode, e);
    }
  }

  public LogPersistence newProcessorInstance() {
    return this.builder.build();
  }

  public String getProcessorType() {
    return this.persistenceType;
  }
}
