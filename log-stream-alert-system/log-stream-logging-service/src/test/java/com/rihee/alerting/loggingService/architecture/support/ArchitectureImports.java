package com.rihee.alerting.loggingService.architecture.support;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.nio.file.Paths;

public final class ArchitectureImports {

  private ArchitectureImports() {}

  public static JavaClasses imports() {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .withImportOption(new ImportOption.DoNotIncludeArchives())
        .withImportOption(new ImportOption.DoNotIncludePackageInfos())
        .importPaths(
            Paths.get("build", "classes", "java", "main").toAbsolutePath(),
            Paths.get("Annotation-Processor", "build", "classes", "java", "main").toAbsolutePath()
        );
  }
}
