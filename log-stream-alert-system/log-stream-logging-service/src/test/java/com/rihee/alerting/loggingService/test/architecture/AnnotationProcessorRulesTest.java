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

public class AnnotationProcessorRulesTest {

  private static final JavaClasses CLASSES = ArchitectureImports.imports();

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
