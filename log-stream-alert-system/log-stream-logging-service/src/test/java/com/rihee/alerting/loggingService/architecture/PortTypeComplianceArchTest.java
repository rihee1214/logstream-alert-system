package com.rihee.alerting.loggingService.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.rihee.alerting.loggingService.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.toos.annotationprocessor.PersistenceTypeProcessor;
import com.rihee.alerting.loggingService.toos.constants.ProcessorRegistryPaths;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PortTypeComplianceArchTest {

  /**
   * ArchUnit 클래스 로딩 방식 비교 결과:
   * - @AnalyzeClasses(패키지 스캔): ~1,000ms ±50ms
   * - importPaths(build/classes/java/main): <100ms
   * 멀티모듈/외부 JAR 스캔을 피하기 위해 importPaths 전략을 채택한다.
   * (성능 수치는 로컬 측정 기준, 환경에 따라 변동 가능)
   */
  private static final JavaClasses IMPORT_LOCAL_MAIN_CLASSES = new ClassFileImporter()
      .withImportOption(new ImportOption.DoNotIncludeTests())
      .withImportOption(new ImportOption.DoNotIncludeJars())
      .withImportOption(new ImportOption.DoNotIncludeArchives())
      .withImportOption(new ImportOption.DoNotIncludePackageInfos())
      .importPaths(Paths.get("build", "classes", "java", "main"));

  static Stream<PortSpec> specs() {
    return Stream.of(
        new PortSpec(
            "collector",
            com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort.class,
            "com.rihee.alerting.loggingService.adapter.in.collector..",
            com.rihee.alerting.loggingService.annotations.CollectorType.class,
            com.rihee.alerting.loggingService.toos.annotationprocessor.CollectorTypeProcessor.class,
            ProcessorRegistryPaths.COLLECTOR.getDeclaringClass()
        ),
        new PortSpec(
            "validator",
            com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort.class,
            "com.rihee.alerting.loggingService.adapter.rule.validator..",
            com.rihee.alerting.loggingService.annotations.ValidatorType.class,
            com.rihee.alerting.loggingService.toos.annotationprocessor.ValidatorTypeProcessor.class,
            ProcessorRegistryPaths.VALIDATOR.getDeclaringClass()
        ),
        new PortSpec(
            "persistence",
            com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort.class,
            "com.rihee.alerting.loggingService.adapter.out.persistence..",
            com.rihee.alerting.loggingService.annotations.PersistenceType.class,
            PersistenceTypeProcessor.class,
            ProcessorRegistryPaths.PERSISTENCE.getDeclaringClass()
        )
    );
  }

  @ParameterizedTest
  @MethodSource("specs")
  void adapters_must_implement_port_and_have_annotation(PortSpec s) {
    ArchRule r = classes()
        .that().resideInAnyPackage(s.adapterPackagePattern())
        .and().areTopLevelClasses().and().areNotInterfaces()
        .should().beAssignableTo(s.portInterface())
        .andShould().beAnnotatedWith(s.adapterAnnotation());
    r.check(IMPORT_LOCAL_MAIN_CLASSES);
  }
}
