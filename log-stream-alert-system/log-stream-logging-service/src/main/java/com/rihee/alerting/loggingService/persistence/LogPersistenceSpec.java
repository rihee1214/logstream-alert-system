package com.rihee.alerting.loggingService.persistence;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.util.Properties;

public class LogPersistenceSpec {

  private final Properties setting;

  private LogPersistenceSpec(Properties setting) {
    this.setting = setting;
  }

  public static LogPersistenceSpec from(Properties setting) {
    return new LogPersistenceSpec(setting);
  }

  /*
   * TODO [설정 기반 객체 생성 로직 개선]
   *  현재 코드는 단순히 조건에 맞는 클래스를 선택하는 수준임.
   *  아래 기능을 추가로 구현해야 함:
   *  1. Properties에 정의된 키-값 항목을 기반으로,
   *    해당 클래스의 필드 중 @CollectorProperty 등의 어노테이션이 붙은 항목에 자동 주입되도록 구현할 것.
   *    (필요 시 타입 변환 및 누락 필드에 대한 예외 처리도 포함)
   *  2. 객체 생성을 위한 팩토리 메서드를 별도로 만들 것.
   *    단, 절대로 싱글턴으로 만들어 모든 worker에서 공유되면 안 되며,
   *    각 worker별로 새로운 인스턴스가 생성되어야 함 (상태 공유 금지).
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends LogPersistence> resolvePersistence(String persistenceMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages("com.rihee.alerting.loggingService.persistence.impl") // 스캔 범위 제한
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
}
