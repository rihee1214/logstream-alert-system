package com.rihee.alerting.loggingService.validators;

import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.core.LogProcessorSpec;
import com.rihee.alerting.loggingService.validators.LogValidator.Builder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class LogValidatorSpec implements LogProcessorSpec {

  private final Builder<?> builder;
  private final String validatorType;

  public LogValidatorSpec(Map<String, String> setting) {
    this.validatorType = setting.get("validator.type");
    if (this.validatorType == null || this.validatorType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'validator.type' 이 존재하지 않습니다.");
    }

    this.builder = resolveValidatorBuilder(this.validatorType)
                                .withProperties(setting);
  }

  @SuppressWarnings("unchecked")
  private static LogValidator.Builder<?> resolveValidatorBuilder(String validatorMode) {
    try (ScanResult scanResult = new ClassGraph()
        .enableAllInfo()
        .acceptPackages("com.rihee.alerting.loggingService.validators.impl")
        .scan()) {

      // ValidatorType annotation과 일치하는 클래스 찾기
      Class<? extends LogValidator> validatorClass = scanResult
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
          .map(clazz -> (Class<? extends LogValidator>) clazz)
          .findFirst()
          .orElseThrow(() ->
              new IllegalStateException("해당 validatorMode에 맞는 클래스가 존재하지 않습니다: " + validatorMode));

      // static builder() 메서드 호출
      Method builderMethod = validatorClass.getDeclaredMethod("builder");
      if (!Modifier.isStatic(builderMethod.getModifiers())) {
        throw new IllegalStateException("builder() 메서드는 static이어야 합니다.");
      }

      return (LogValidator.Builder<?>) builderMethod.invoke(null);

    } catch (Exception e) {
      throw new RuntimeException("Validator 빌더 생성 실패: " + validatorMode, e);
    }
  }

  public LogValidator newProcessorInstance() {
    return builder.build();
  }

  public String getProcessorType() {
    return this.validatorType;
  }
}
