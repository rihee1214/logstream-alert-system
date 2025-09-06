package com.rihee.alerting.loggingService.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.test.architecture.support.ArchitectureImports;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AdapterAnnotationRulesTest {

  private static final JavaClasses CLASSES = ArchitectureImports.imports();

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
