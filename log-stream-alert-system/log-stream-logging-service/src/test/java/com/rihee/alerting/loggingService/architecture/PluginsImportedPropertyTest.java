package com.rihee.alerting.loggingService.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeArchives;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludePackageInfos;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.rihee.alerting.loggingService.plugins",
    importOptions = {DoNotIncludeTests.class,
        DoNotIncludeArchives.class,
        DoNotIncludePackageInfos.class}
)
public class PluginsImportedPropertyTest {

//  @ArchTest
//  static final ArchRule plugins_use_adapter_class =
//      classes()
//          .that().resideInAnyPackage("com.rihee.alerting.loggingService.plugin")
//          .and().areTopLevelClasses()
//          .should().dependOnClassesThat()
//          .resideInAnyPackage("com.rihee.alerting.loggingService.adapter..");
}
