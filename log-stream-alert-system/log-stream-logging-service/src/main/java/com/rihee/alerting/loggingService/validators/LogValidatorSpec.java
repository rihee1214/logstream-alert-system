package com.rihee.alerting.loggingService.validators;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.validators.LogValidator.Builder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class LogValidatorSpec {

  private final Builder<?> builder;

  private LogValidatorSpec(Properties setting) {
    this.builder = resolveValidatorBuilder(setting.getProperty("validator.type"))
                                .withProperties(MapUtils.toMap(setting));
  }

  public static LogValidatorSpec from(Properties setting) {
    return new LogValidatorSpec(setting);
  }

  @SuppressWarnings("unchecked")
  private static LogValidator.Builder<?> resolveValidatorBuilder(String validatorMode) {
    if (StringUtils.isEmpty(validatorMode)) {
      throw new IllegalArgumentException("Validator 설정이 존재하지 않습니다.");
    }

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


  public LogValidator newValidatorInstance() {
    return builder.build();
  }
}
