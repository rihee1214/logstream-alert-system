package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort.Builder;
import com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * {@code LogValidatorPlugin}은 설정에서 지정된 {@code validator.type} 값을 기반으로
 * 해당 타입의 {@link LogValidatorPort} 구현체를 탐색하고, 해당 구현체의 {@code builder()} 메서드를
 * 호출하여 검증기 인스턴스를 생성하는 팩토리 역할을 수행합니다.
 *
 * <p>구체 클래스 탐색은 {@link ValidatorType} 애노테이션을 기반으로 하며,
 * 구현체는 {@link #VALIDATOR_NAMESPACE} 패키지 내에서 검색됩니다.
 *
 * <p>이 클래스는 런타임에 리플렉션과 {@link ClassGraph}를 사용하여 동적으로 인스턴스를 생성합니다.
 *
 * @see LogValidatorPort
 * @see ValidatorType
 * @see com.rihee.alerting.loggingService.adapter.rule.validator
 */
public final class LogValidatorPlugin implements LogProcessorPlugin {

  /**
   * {@link LogValidatorPort} 구현체를 탐색할 대상 패키지 경로.
   *
   * <p>{@link #resolveValidatorBuilder(String)} 메서드에서 {@link ClassGraph}를 사용해
   * 이 패키지 내부의 클래스를 검색하고, {@link ValidatorType} 애노테이션이 붙은 구현체를 찾는다.
   */
  private static final String VALIDATOR_NAMESPACE
                            = "com.rihee.alerting.loggingService.adapter.rule.validator";

  private final Builder<?> builder;
  private final String validatorType;

  /**
   * 주어진 설정 맵을 기반으로 {@link LogValidatorPlugin}를 생성합니다.
   *
   * <p>설정에서 {@code validator.type} 키를 읽어 해당 타입에 맞는
   * {@link LogValidatorPort} 구현체를 찾고, 해당 구현체의 빌더를 초기화합니다.
   *
   * @param setting 검증기 생성을 위한 설정 맵 (필수 키: {@code validator.type})
   * @throws IllegalArgumentException {@code validator.type} 값이 없거나 비어 있을 경우
   * @throws RuntimeException 해당 타입에 맞는 검증기 빌더를 찾거나 생성하지 못한 경우
   */
  public LogValidatorPlugin(Map<String, String> setting) {
    this.validatorType = setting.get("validator.type");
    if (this.validatorType == null || this.validatorType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'validator.type' 이 존재하지 않습니다.");
    }

    this.builder = resolveValidatorBuilder(this.validatorType)
                                .withProperties(setting);
  }

  /**
   * 지정된 {@code validatorMode} 값에 해당하는 {@link LogValidatorPort} 구현체의
   * 빌더를 탐색하고 반환합니다.
   *
   * <p>탐색 방식:
   * <ol>
   *   <li>{@link #VALIDATOR_NAMESPACE} 패키지를 스캔</li>
   *   <li>{@link ValidatorType} 애노테이션이 붙은 클래스를 필터링</li>
   *   <li>애노테이션 값이 {@code validatorMode}와 일치하는 클래스 선택</li>
   *   <li>선택된 클래스의 static {@code builder()} 메서드 호출</li>
   * </ol>
   *
   * @param validatorMode 찾고자 하는 검증기 타입 식별자
   * @return 해당 타입의 {@link LogValidatorPort.Builder}
   * @throws IllegalStateException builder 메서드가 없거나 static이 아닌 경우,
   *                               혹은 구현체를 찾지 못한 경우
   * @throws RuntimeException 리플렉션 또는 빌더 생성 과정에서 오류가 발생한 경우
   */
  @SuppressWarnings("unchecked")
  private static LogValidatorPort.Builder<?> resolveValidatorBuilder(String validatorMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages(VALIDATOR_NAMESPACE)
        .scan()) {

      // ValidatorType annotation과 일치하는 클래스 찾기
      Class<? extends LogValidatorPort> validatorClass = scanResult
          .getClassesWithAnnotation(ValidatorType.class.getName())
          .stream()
          .map(classInfo -> {
            try {
              return (Class<?>) Class.forName(classInfo.getName());
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .filter(clazz -> {
            ValidatorType annotation = clazz.getAnnotation(ValidatorType.class);
            return annotation != null && annotation.value().equals(validatorMode);
          })
          .map(clazz -> (Class<? extends LogValidatorPort>) clazz)
          .findFirst()
          .orElseThrow(() ->
              new IllegalStateException("해당 validatorMode에 맞는 클래스가 존재하지 않습니다: " + validatorMode));

      // static builder() 메서드 호출
      Method builderMethod = validatorClass.getDeclaredMethod("builder");
      if (!Modifier.isStatic(builderMethod.getModifiers())) {
        throw new IllegalStateException("builder() 메서드는 static이어야 합니다.");
      }

      return (LogValidatorPort.Builder<?>) builderMethod.invoke(null);

    } catch (Exception e) {
      throw new RuntimeException("Validator 빌더 생성 실패: " + validatorMode, e);
    }
  }

  /**
   * 현재 스펙에 맞는 {@link LogProcessorPort} 인스턴스를 생성합니다.
   *
   * <p>빌더 패턴을 사용하여 {@link LogValidatorPort} 구현체 인스턴스를 생성하며,
   * 생성된 인스턴스는 런타임 파이프라인에서 사용됩니다.
   *
   * @return 새 {@link LogProcessorPort} 인스턴스
   */
  public LogProcessorPort newProcessorInstance() {
    return builder.build();
  }

  /**
   * 현재 스펙이 나타내는 검증기 타입 식별자를 반환합니다.
   *
   * @return 설정에서 지정된 {@code validator.type} 값
   */
  public String getProcessorType() {
    return this.validatorType;
  }
}
