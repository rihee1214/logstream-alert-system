package com.rihee.alerting.loggingService.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class PortsAnnotatedPropertyTest {

  /**
   * ArchUnit 클래스 로딩 방식 비교 결과:
   * - @AnalyzeClasses(패키지 스캔): ~1,000ms ±50ms
   * - importPaths(build/classes/java/main): <100ms
   * 멀티모듈/외부 JAR 스캔을 피하기 위해 importPaths 전략을 채택한다.
   * (성능 수치는 로컬 측정 기준, 환경에 따라 변동 가능)
   */
  private static final JavaClasses importLocalMainClasses = new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .withImportOption(new ImportOption.DoNotIncludeJars())
          .withImportOption(new ImportOption.DoNotIncludeArchives())
          .withImportOption(new ImportOption.DoNotIncludePackageInfos())
          .importPaths(Paths.get("build", "classes", "java", "main"));

  /**
   * adapter Package안에 있는 Collector 클래스는 모두 CollectorAdapter로 끝나야 한다.
   */
  @Test
  void collectorAdaptersShouldEndWithCollectorAdapter() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter..collector..")
        .and().areTopLevelClasses()
        .should().haveSimpleNameEndingWith("CollectorAdapter")
        .as("adapter Package안에 있는 collector 패키지의 클래스는 모두 CollectorAdapter로 끝나야 한다.");

    rule.check(importLocalMainClasses);
  }

  /**
   * adapter Package안에 있는 validator 클래스는 모두 ValidatorAdapter로 끝나야 한다.
   */
  @Test
  void validatorAdaptersShouldEndWithValidatorAdapter() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter..validator..")
        .and().areTopLevelClasses()
        .should().haveSimpleNameEndingWith("ValidatorAdapter")
        .as("adapter Package안에 있는 validator 패키지의 클래스는 모두 ValidatorAdapter로 끝나야 한다.");

    rule.check(importLocalMainClasses);
  }

  /**
   * adapter Package안에 있는 persistence 클래스는 모두 PersistenceAdapter로 끝나야 한다.
   */
  @Test
  void persistenceAdaptersShouldEndWithPersistenceAdapter() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter..persistence..")
        .and().areTopLevelClasses()
        .should().haveSimpleNameEndingWith("PersistenceAdapter")
        .as("adapter Package안에 있는 persistence 패키지의 클래스는 모두 PersistenceAdapter로 끝나야 한다.");

    rule.check(importLocalMainClasses);
  }

  @Test
  void collectorsShouldHavePropertyAnnotation() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.in.collector..")
        .and().areTopLevelClasses()
        .should().beAnnotatedWith(CollectorType.class)
        .as("Collector Package안에 있는 클래스는 모두 CollectorType Annotation을 가지고 있어야 한다.");

    rule.check(importLocalMainClasses);
  }

  @Test
  void validatorsShouldHavePropertyAnnotation() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.rule.validator..")
        .and().areTopLevelClasses()
        .should().beAnnotatedWith(ValidatorType.class);

    rule.check(importLocalMainClasses);
  }

  @Test
  void persistenceShouldHavePropertyAnnotation() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.out.persistence..")
        .and().areTopLevelClasses()
        .should().beAnnotatedWith(PersistenceType.class);

    rule.check(importLocalMainClasses);
  }
}
