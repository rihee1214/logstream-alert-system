package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort.Builder;
import com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort;
import com.rihee.alerting.loggingService.toos.constants.ProcessorRegistryPaths;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
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

  private static LogValidatorPort.Builder<?> resolveValidatorBuilder(String validatorMode) {
    final String filePath = ProcessorRegistryPaths.VALIDATOR.getFilePath();
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();

    // 1) 레지스트리 로딩
    final Map<String, Object> classInfos;
    try (InputStream is = cl.getResourceAsStream(filePath)) {
      if (is == null) {
        throw new IllegalStateException("Registry resource not found: " + filePath);
      }
      // Map<String, ?> 대신 문자열 맵 강제 (JSON 규약: 키는 문자열)
      classInfos = MapUtils.fromInputStream(is);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read registry: " + filePath, e);
    }

    // 2) 대상 FQCN 조회
    final String fqcn = (String) classInfos.get(validatorMode);
    if (StringUtils.isBlank(fqcn)) {
      throw new IllegalArgumentException("Unknown validator mode: " + validatorMode
          + " (in " + filePath + ")");
    }

    // 3) 클래스 로딩
    final Class<?> validatorClass;
    try {
      validatorClass = Class.forName(fqcn, /*initialize*/ false, cl);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Validator class not found: " + fqcn, e);
    }

    // 4) builder() 확인 및 호출
    try {
      // public static builder()
      Method builderMethod = validatorClass.getMethod("builder");

      if (!LogProcessorPort.Builder.class.isAssignableFrom(builderMethod.getReturnType())) {
        throw new IllegalStateException(fqcn + ".builder() must return LogProcessorPort.Builder");
      }

      return (LogProcessorPort.Builder<?>) builderMethod.invoke(null);

    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Missing public static builder() in " + fqcn, e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Failed to invoke builder() in " + fqcn, e);
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
