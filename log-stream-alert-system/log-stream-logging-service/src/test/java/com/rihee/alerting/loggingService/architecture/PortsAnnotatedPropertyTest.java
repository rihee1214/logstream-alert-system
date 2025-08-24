package com.rihee.alerting.loggingService.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeArchives;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludePackageInfos;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.rihee.alerting.loggingService.adapter",
    importOptions = {DoNotIncludeTests.class,
        DoNotIncludeArchives.class,
        DoNotIncludePackageInfos.class}
)
public class PortsAnnotatedPropertyTest {

  /**
   * adapter Package안에 있는 클래스는 모두 Adapter로 끝나야 한다.
   */
  @ArchTest
  static final ArchRule adapters_should_end_with_Adapter =
      classes()
          .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter..")
          .and().areTopLevelClasses()
          .should().haveSimpleNameEndingWith("Adapter");

  @ArchTest
  static final ArchRule collectors_should_have_property_annotation =
      classes()
          .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.in.collector..")
          .and().areTopLevelClasses()
          .should().beAnnotatedWith(CollectorType.class);

  @ArchTest
  static final ArchRule validators_should_have_property_annotation =
      classes()
          .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.rule.validator..")
          .and().areTopLevelClasses()
          .should().beAnnotatedWith(ValidatorType.class);

  @ArchTest
  static final ArchRule persistence_should_have_property_annotation =
      classes()
          .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.out.persistence..")
          .and().areTopLevelClasses()
          .should().beAnnotatedWith(PersistenceType.class);
}
