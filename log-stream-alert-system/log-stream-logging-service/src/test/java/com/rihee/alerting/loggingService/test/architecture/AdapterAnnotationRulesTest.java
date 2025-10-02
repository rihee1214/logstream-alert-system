package com.rihee.alerting.loggingService.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.test.architecture.support.ArchitectureImports;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 아키텍처 테스트: 모든 Adapter 클래스가 올바른 포트-어댑터 규약을 준수하는지 검증한다.
 *
 * <p>이 규약은 헥사고날 아키텍처(Ports & Adapters) 원칙에 기반하며,
 * 각 어댑터는 반드시 자신의 추상 Port를 구현하고, 대응되는 어노테이션을 보유해야 한다.
 *
 * <h2>검증 대상 규칙</h2>
 * <ul>
 *   <li>Adapter 클래스는 지정된
 *      {@link com.rihee.alerting.loggingService.core.pipeline.port} 추상 Port 타입을 구현해야 한다.</li>
 *   <li>Adapter 클래스는 대응되는 식별 어노테이션을 보유해야 한다.
 *        (예: {@code @CollectorType}, {@code @ValidatorType}, {@code @PersistenceType})</li>
 *   <li>Adapter 클래스는 반드시 최상위 클래스(top-level)이어야 하며, 인터페이스가 아니어야 한다.</li>
 * </ul>
 *
 * @see PortSpec
 * @see ArchitectureImports
 * @see com.tngtech.archunit.lang.syntax.ArchRuleDefinition
 */
public class AdapterAnnotationRulesTest {

  /**
   * 테스트 대상 JavaClasses를 import하는 공통 지원 클래스.
   */
  private static final JavaClasses CLASSES = ArchitectureImports.imports();

  /**
   * {@link PortSpec}의 스트림을 제공하여 파라미터라이즈드 테스트로 반복 검증한다.
   *
   * @param s 각 포트/어댑터 타입의 메타정보(어댑터 패키지 패턴, 대응 추상 Port, 요구 어노테이션)를 정의.
   */
  @ParameterizedTest(name = "{0} adapters must implement abstract port and have annotation")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void adaptersMustImplementPortAndHaveAnnotation(PortSpec s) {
    classes()
        .that().resideInAnyPackage(s.adapterPackagePattern())
        .and().areTopLevelClasses()
        .and().areNotInterfaces()
        .should().beAssignableTo(s.portAbstracts())
        .andShould().beAnnotatedWith(s.adapterAnnotation())
        .check(CLASSES);
  }
}
