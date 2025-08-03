package com.rihee.alerting.loggingService.collectors;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor.Builder;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessorSpec;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * {@code LogCollectorSpec}은 설정 정보를 기반으로 {@link LogCollector}의 구체 구현체를
 * 동적으로 탐색하고, 해당 구현체의 Builder를 통해 인스턴스를 생성하는 책임을 갖는다.
 *
 * <p>Collector는 {@link CollectorType} 애노테이션으로 타입을 식별하며,
 * {@code collector.type} 설정 값을 기준으로 일치하는 클래스를 탐색한다.
 * 탐색된 클래스는 반드시 {@code public static builder()} 메서드를 제공해야 하며,
 * 해당 메서드를 통해 {@link com.rihee.alerting.loggingService.core.pipeline.LogProcessor.Builder} 인스턴스를
 * 획득하여 설정을 주입하고 Collector를 생성한다.
 *
 * <p>이 클래스는 초기화 시 1회의 설정만 받고, 이후 Collector 인스턴스를 반복 생성할 수 있도록 설계되었다.
 *
 * @author 리희
 * @since 1.0
 */
public final class LogCollectorSpec implements LogProcessorSpec {

  private final Builder<?> builder;
  private final String collectorType;

  public LogCollectorSpec(Map<String, String> setting) {
    this.collectorType = setting.get("collector.type");
    if (this.collectorType == null || this.collectorType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'collector.type' 이 존재하지 않습니다.");
    }

    this.builder = resolveCollectorBuilder(collectorType)
                                  .withProperties(setting);
  }

  @Override
  public LogProcessor newProcessorInstance() {
    return builder.build();
  }

  /**
   * {@code collector.type} 값에 해당하는 {@link LogCollector} 구현체를 찾아
   * 그 내부의 static {@code builder()} 메서드를 호출하여 Builder 인스턴스를 반환한다.
   *
   * <p>이 메서드는 클래스 스캔 및 reflection 기반이므로 예외 발생 가능성이 있다.
   * 다만 AnnotationProcessor를 통해 그럴 가능성을 사전에 차단한다.
   *
   * @param collectorMode 설정 파일에서 정의된 collector type 값
   * @return 해당 구현체의 {@link LogCollector.Builder} 인스턴스
   * @throws IllegalStateException 매칭되는 클래스가 없거나 builder 메서드가 static이 아닐 경우
   * @throws RuntimeException reflection 또는 builder 호출 중 예외가 발생한 경우
   */
  @SuppressWarnings("unchecked")
  private static LogProcessor.Builder<?> resolveCollectorBuilder(String collectorMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages("com.rihee.alerting.loggingService.collectors.impl")
        .scan()) {

      // CollectorType annotation과 일치하는 클래스 찾기
      Class<? extends LogCollector> collectorClass = scanResult
          .getClassesWithAnnotation(CollectorType.class.getName())
          .stream()
          .map(classInfo -> {
            try {
              return (Class<?>) Class.forName(classInfo.getName());
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
          .orElseThrow(() ->
              new IllegalStateException("해당 collectorMode에 맞는 클래스가 존재하지 않습니다: " + collectorMode));

      // builder 메서드 확인 및 호출
      Method builderMethod = collectorClass.getDeclaredMethod("builder");
      if (!Modifier.isStatic(builderMethod.getModifiers())) {
        throw new IllegalStateException("builder() 메서드는 static이어야 합니다.");
      }

      return (LogCollector.Builder<?>) builderMethod.invoke(null);

    } catch (Exception e) {
      throw new RuntimeException("Collector 빌더 생성 실패: " + collectorMode, e);
    }
  }

  public String getProcessorType() {
    return this.collectorType;
  }
}
