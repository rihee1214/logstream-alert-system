package com.rihee.alerting.loggingService.annotationprocessor;

import com.rihee.alerting.loggingService.annotations.ValidatorType;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.SupportedAnnotationTypes;

/**
 * {@code ValidatorTypeProcessor}는
 * {@link ValidatorType}어노테이션이 부여된 클래스에 대해
 * 컴파일 타임 검증을 수행하는 annotation processor입니다.
 *
 * <p>다음과 같은 규칙을 강제합니다:
 * <ul>
 *   <li><b>중복 금지:</b> 동일한 {@code @ValidatorType.value()} 값을 가진 클래스가 둘 이상 존재하면 컴파일 오류를 발생시킵니다.</li>
 *   <li><b>정적 팩토리 메서드 요구:</b> 어노테이션이 부여된 클래스는 반드시 {@code public static builder()} 메서드를 정의해야 하며,
 *       이는 외부에서 인스턴스를 생성하는 진입점으로 사용됩니다.
 *   </li>
 * </ul>
 *
 * <p>이 프로세서는 {@code META-INF/services/javax.annotation.processing.Processor} 파일을 통해
 * 서비스 로딩되어 Java 컴파일러에 의해 자동으로 실행됩니다.
 *
 * <p>이 검사는 런타임 오류를 방지하고, 설정 기반 로그 수집기 로딩 시스템이 안정적으로 동작하기 위한 사전 조건을 보장합니다.
 *
 * <p><b>주의:</b> 이 processor는 반드시 {@code @SupportedAnnotationTypes}에
 * {@code "com.rihee.alerting.loggingService.annotations.ValidatorType"}을 명시해야 하며,
 * Gradle이나 Maven 빌드 시스템에서는 반드시 processor path와 resources 등록이 필요합니다.
 *
 * @see ValidatorType
 * @see AbstractTypeProcessor
 */
@SupportedAnnotationTypes("com.rihee.alerting.loggingService.annotations.ValidatorType")
public class ValidatorTypeProcessor extends AbstractTypeProcessor {

  private final Map<String, String> validatorTypes = new HashMap<>();

  @Override
  protected Class<? extends Annotation> getTargetAnnotationType() {
    return ValidatorType.class;
  }
}
