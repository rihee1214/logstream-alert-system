package com.rihee.alerting.loggingService.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.test.architecture.support.ArchitectureImports;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * PortAbstractionRulesTest 클래스는 포트 추상체에 대한 아키텍처 규칙을 정의하고
 * 해당 규칙이 올바르게 구현되었는지를 검증하는 테스트를 제공하는 클래스입니다.
 *
 * <p>테스트는 JUnit 5를 기반으로 작성되었으며, 주요 검증 항목은 다음과 같습니다.
 *   <ol>
 *     <li>포트 추상체가 LogProcessorPort 인터페이스를 구현해야 한다는 규칙 준수 여부.</li>
 *     <li>각 포트 추상체가 `stage()` 메서드를 반드시 정의하고, 반환 값 및 제약 사항을 충족하는지 여부.</li>
 *   </ol>
 *</p>
 *
 * <p>테스트는 외부에서 제공되는 PortSpec 데이터를 기반으로 동작하며, 각 테스트는 파라미터화되어
 * 다양한 포트 추상체의 규칙 준수를 자동으로 검증합니다.</p>
 *
 * @see LogProcessorPort
 * @see com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures
 */
public class PortAbstractionRulesTest {

  /**
   * 테스트 대상 JavaClasses를 import하는 공통 지원 클래스.
   */
  private static final JavaClasses CLASSES = ArchitectureImports.imports();

  /**
   * 각 포트 추상체가 LogProcessorPort 인터페이스를 구현하는지 확인합니다.
   *
   * @param s 각 포트/어댑터 타입의 메타정보(어댑터 패키지 패턴, 대응 추상 Port, 요구 어노테이션)를 정의.
   */
  @ParameterizedTest(name = "{0} abstract port must implement LogProcessorPort")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void portAbstractShouldBeAssignableToLogProcessorPort(PortSpec s) {
    classes().that().haveFullyQualifiedName(s.portAbstracts().getName())
        .should().beAssignableTo(LogProcessorPort.class)
        .check(CLASSES);
  }

  /**
   * 각 포트 추상체의 `stage()` 메서드가 반환 타입이 `String`이며, 파라미터가 없고 `final` 제약 조건을 준수하는지 검증합니다.
   *
   * @param s 각 포트/어댑터 타입의 메타정보(어댑터 패키지 패턴, 대응 추상 Port, 요구 어노테이션)를 정의.
   * @throws Exception 리플렉션 관련 Exception.
   */
  @ParameterizedTest(name = "{0} abstract port must expose final stage()")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void portAbstractShouldExposeFinalStage(PortSpec s) throws Exception {
    Method m = s.portAbstracts().getDeclaredMethod("stage");
    assertEquals(String.class, m.getReturnType(), "stage() must return String");
    assertEquals(0, m.getParameterCount(), "stage() must have no params");
    assertTrue(Modifier.isFinal(m.getModifiers()), "stage() must be final");
  }
}
