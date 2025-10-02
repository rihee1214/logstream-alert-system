package com.rihee.alerting.loggingService.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.test.architecture.support.ArchitectureImports;
import com.rihee.alerting.loggingService.tools.annotationprocessor.AbstractTypeProcessor;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * AnnotationProcessorRulesTest 클래스는 애노테이션 프로세서의 구조적 규칙 및 애노테이션 유효성을 검증하는 테스트를 정의합니다.
 * 이를 통해 애노테이션 프로세서가 특정 규약을 준수하는지 확인합니다.
 *
 * <p>주요 테스트 시나리오:
 * <ol>
 *   <li>애노테이션 프로세서의 이름과 상속 구조가 올바른지 확인.</li>
 *   <li>애노테이션 프로세서가 지정된 애노테이션과 소스 버전을 올바르게 처리하는지 확인.</li>
 * </ol>
 *</p>
 *
 * <p>사용된 주요 메서드:
 * <ol>
 *   <li>annotationProcessorMustBeTypedCorrectly(PortSpec):
 *     <ul>
 *       <li></li>
 *     </ul>
 *   </li>
 *   <li>validateAnnotationProcessorsAnnotation(PortSpec):
 *    <ul>
 *      <li></li>
 *    </ul>
 *   </li>
 * </ol>
 * </p>
 *
 * <p>이 클래스는 JUnit 5 및 ArchUnit을 활용하여 작성되었으며,
 * {@code @ParameterizedTest}와 {@code @MethodSource}를 사용하여 다양한 포트 사양(PortSpec)에 대한 테스트를 실행합니다.
 */
public class AnnotationProcessorRulesTest {

  /**
   * 테스트 대상 JavaClasses를 import하는 공통 지원 클래스.
   */
  private static final JavaClasses CLASSES = ArchitectureImports.imports();

  /**
   * 애노테이션 프로세서가 AbstractTypeProcessor를 상속받고, 올바른 명명 규칙을 따르는지 확인합니다.
   *
   * @param s target port specification.
   */
  @ParameterizedTest(name = "{0} AP should extend AbstractTypeProcessor and follow naming")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void annotationProcessorMustBeTypedCorrectly(PortSpec s) {
    classes()
        .that().haveFullyQualifiedName(s.annotationProcessor().getName())
        .should().beAssignableTo(AbstractTypeProcessor.class)
        .andShould().haveSimpleNameStartingWith(s.name())
        .andShould().haveSimpleNameEndingWith("TypeProcessor")
        .check(CLASSES);
  }

  /**
   * 애노테이션 프로세서의 반환 값 및 애노테이션(@SupportedAnnotationTypes, @SupportedSourceVersion)이
   * 사양과 일치하는지 확인합니다.
   *
   * @param s target port specification.
   * @throws Exception 리플렉션 관련 Exception.
   */
  @ParameterizedTest(name = "{0} AP annotations must match spec")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void validateAnnotationProcessorsAnnotation(PortSpec s) throws Exception {
    Class<?> clazz = s.annotationProcessor();

    Method m = clazz.getDeclaredMethod("getTargetAnnotationType");
    m.setAccessible(true);
    @SuppressWarnings("unchecked")
    Class<? extends Annotation> actual = (Class<? extends Annotation>)
        m.invoke(clazz.getDeclaredConstructor().newInstance());
    assertEquals(s.adapterAnnotation(), actual,
        "getTargetAnnotationType() must return spec.adapterAnnotation()");

    SupportedAnnotationTypes sat = clazz.getAnnotation(SupportedAnnotationTypes.class);
    assertNotNull(sat);
    assertArrayEquals(new String[]{s.adapterAnnotation().getName()}, sat.value(),
        "@SupportedAnnotationTypes must equal the FQN of adapterAnnotation");

    SupportedSourceVersion ssv = clazz.getAnnotation(SupportedSourceVersion.class);
    assertNotNull(ssv);
    assertEquals(SourceVersion.RELEASE_21, ssv.value(),
        "@SupportedSourceVersion must be RELEASE_21");
  }
}
