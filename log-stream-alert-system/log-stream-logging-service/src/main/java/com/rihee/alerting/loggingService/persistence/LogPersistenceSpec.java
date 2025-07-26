package com.rihee.alerting.loggingService.persistence;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.persistence.LogPersistence.Builder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class LogPersistenceSpec {

  private final Builder<?> builder;

  private LogPersistenceSpec(Properties setting) {
    this.builder = resolvePersistenceBuilder(setting.getProperty(""))
                            .withProperties(MapUtils.toMap(setting));
  }

  public static LogPersistenceSpec from(Properties setting) {
    return new LogPersistenceSpec(setting);
  }

  @SuppressWarnings("unchecked")
  private static LogPersistence.Builder<?> resolvePersistenceBuilder(String persistenceMode) {
    if (StringUtils.isEmpty(persistenceMode)) {
      throw new IllegalArgumentException("Persistence 설정이 존재하지 않습니다.");
    }

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

  public LogPersistence newPersistenceInstance() {
    return this.builder.build();
  }

}
