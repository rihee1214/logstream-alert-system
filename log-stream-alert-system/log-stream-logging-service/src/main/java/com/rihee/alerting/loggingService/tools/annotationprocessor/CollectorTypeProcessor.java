package com.rihee.alerting.loggingService.tools.annotationprocessor;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import java.lang.annotation.Annotation;
import javax.annotation.processing.SupportedAnnotationTypes;

/**
 * {@code CollectorTypeProcessor}는 {@link CollectorType} 애너테이션이 부여된 클래스들을 대상으로
 * 컴파일 타임에서의 검증을 수행하는 Annotation Processor입니다.
 *
 * <p>이 클래스는 {@link AbstractTypeProcessor}를 상속받아 공통 로직을 재사용하며,
 * {@link #getTargetAnnotationType()}을 통해 처리 대상 애너테이션을 {@code CollectorType.class}로 지정합니다.
 * 구체적인 검증 로직은 상위 추상 클래스 내의 {@link ProcessorLogic} 구현체들을 통해 수행됩니다.
 *
 * <h3>기본 제공되는 검증 규칙:</h3>
 * <ul>
 *   <li><b>중복 식별자 검사:</b> 동일한 {@code @CollectorType.value()} 값을 갖는 클래스가
 *                              둘 이상 존재할 경우 컴파일 오류를 발생시킵니다.</li>
 *   <li><b>정적 빌더 메서드 검사:</b> 해당 클래스는 반드시 {@code public static builder()} 메서드를 포함해야 하며,
 *       이를 통해 외부에서 객체 생성을 유도할 수 있어야 합니다.</li>
 * </ul>
 *
 * <h3>상속 구조의 역할 분리:</h3>
 * <ul>
 *   <li>이 클래스는 <strong>어떤 애너테이션을 검사할 것인지 지정</strong>하는 역할만 수행합니다.</li>
 *   <li>실제 처리 로직은 {@link AbstractTypeProcessor} 내의 {@code ProcessorLogic} 인터페이스 기반 구현에 위임됩니다.</li>
 *   <li>필요 시 사용자 정의 로직을 {@code processorLogics} 리스트에 추가하여 확장할 수 있습니다.</li>
 * </ul>
 *
 * <h3>빌드 시스템 통합 시 주의사항:</h3>
 * <ul>
 *   <li>{@code META-INF/services/javax.annotation.processing.Processor} 파일을 통해 등록되어야 합니다.</li>
 *   <li>Gradle 또는 Maven에서 annotation processor path가 명확히 지정되어 있어야 정상 작동합니다.</li>
 * </ul>
 *
 * @see CollectorType
 * @see AbstractTypeProcessor
 * @see javax.annotation.processing.Processor
 */
@SupportedAnnotationTypes("com.rihee.alerting.loggingService.annotations.CollectorType")
public class CollectorTypeProcessor extends AbstractTypeProcessor {

  @Override
  protected Class<? extends Annotation> getTargetAnnotationType() {
    return CollectorType.class;
  }
}
