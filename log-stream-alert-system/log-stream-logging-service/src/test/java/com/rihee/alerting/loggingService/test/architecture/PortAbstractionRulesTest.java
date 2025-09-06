package com.rihee.alerting.loggingService.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.test.architecture.support.ArchitectureImports;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PortAbstractionRulesTest {

  private static final JavaClasses CLASSES = ArchitectureImports.imports();

  @ParameterizedTest(name = "{0} abstract port must implement LogProcessorPort")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void portAbstractShouldBeAssignableToLogProcessorPort(PortSpec s) {
    classes().that().haveFullyQualifiedName(s.portAbstracts().getName())
        .should().beAssignableTo(LogProcessorPort.class)
        .check(CLASSES);
  }

  @ParameterizedTest(name = "{0} abstract port must expose final stage()")
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void portAbstractShouldExposeFinalStage(PortSpec s) throws Exception {
    Method m = s.portAbstracts().getDeclaredMethod("stage");
    assertEquals(String.class, m.getReturnType(), "stage() must return String");
    assertEquals(0, m.getParameterCount(), "stage() must have no params");
    assertTrue(Modifier.isFinal(m.getModifiers()), "stage() must be final");
  }
}
