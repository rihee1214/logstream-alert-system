package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessor.Builder;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public final class LogPersistencePlugin implements LogProcessorPlugin {

  private static final String PERSISTENCE_NAMESPACE
                      = "com.rihee.alerting.loggingService.adapter.out.persistence";

  private final Builder<?> builder;
  private final String persistenceType;

  public LogPersistencePlugin(Map<String, String> setting) {
    this.persistenceType = setting.get("persistence.type");
    if (this.persistenceType == null || this.persistenceType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'persistence.type' 이 존재하지 않습니다.");
    }

    this.builder = resolvePersistenceBuilder(this.persistenceType)
                            .withProperties(setting);
  }

  @SuppressWarnings("unchecked")
  private static LogPersistencePort.Builder<?> resolvePersistenceBuilder(String persistenceMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages(PERSISTENCE_NAMESPACE) // 스캔 범위 제한
        .scan()) {

      // PersistenceType annotation과 일치하는 클래스 찾기
      Class<? extends LogPersistencePort> persistenceClass = scanResult
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
          .map(clazz -> (Class<? extends LogPersistencePort>) clazz)
          .findFirst()
          .orElseThrow(() ->
              new IllegalStateException("해당 persistenceMode에 맞는 클래스가 존재하지 않습니다: " + persistenceMode));

      // static builder() 메서드 존재 여부 확인 및 호출
      Method builderMethod = persistenceClass.getDeclaredMethod("builder");
      if (!Modifier.isStatic(builderMethod.getModifiers())) {
        throw new IllegalStateException("builder() 메서드는 static이어야 합니다.");
      }

      return (LogPersistencePort.Builder<?>) builderMethod.invoke(null);

    } catch (Exception e) {
      throw new RuntimeException("Persistence 빌더 생성 실패: " + persistenceMode, e);
    }
  }

  public LogProcessor newProcessorInstance() {
    return this.builder.build();
  }

  public String getProcessorType() {
    return this.persistenceType;
  }
}
