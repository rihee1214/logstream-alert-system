package com.rihee.alerting.loggingService.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class PluginsImportedPropertyTest {

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

  private static ArchCondition<JavaClass> notAnnotatedWithAnyOtherFromSamePackageAs(Class<?> allowed) {
    return new ArchCondition<>("not annotated with any other annotation from " + allowed.getPackageName()) {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        String pkg = allowed.getPackageName();
        String allowedFqn = allowed.getName();

        for (JavaAnnotation<?> ann : item.getAnnotations()) {
          JavaClass annType = ann.getRawType();
          boolean isSamePkg = pkg.equals(annType.getPackageName());
          boolean isOther   = !allowedFqn.equals(annType.getName());
          if (annType.isAnnotation() && isSamePkg && isOther) {
            String msg = item.getName()
                + " is annotated with disallowed @" + annType.getSimpleName()
                + " (only @" + allowed.getSimpleName() + " is permitted from " + pkg + ")";
            events.add(SimpleConditionEvent.violated(item, msg));
          }
        }
      }
    };
  }

  @Test
  void collectorsShouldHaveOnlyCollectorType() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.in.collector..")
        .and().areTopLevelClasses()
        .should().beAnnotatedWith(CollectorType.class)
        .andShould(notAnnotatedWithAnyOtherFromSamePackageAs(CollectorType.class))
        .as("Collector 패키지의 클래스는 @CollectorType만(같은 패키지의 다른 애노테이션 금지) 가져야 한다");

    rule.check(importLocalMainClasses);
  }

  @Test
  void validators_should_have_only_ValidatorType() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.rule.validator..")
        .and().areTopLevelClasses()
        .should().beAnnotatedWith(ValidatorType.class)
        .andShould(notAnnotatedWithAnyOtherFromSamePackageAs(ValidatorType.class))
        .as("Validator 패키지의 클래스는 @ValidatorType만 가져야 한다");

    rule.check(importLocalMainClasses);
  }

  @Test
  void persistence_should_have_only_PersistenceType() {
    ArchRule rule = classes()
        .that().resideInAnyPackage("com.rihee.alerting.loggingService.adapter.out.persistence..")
        .and().areTopLevelClasses()
        .should().beAnnotatedWith(PersistenceType.class)
        .andShould(notAnnotatedWithAnyOtherFromSamePackageAs(PersistenceType.class))
        .as("Persistence 패키지의 클래스는 @PersistenceType만 가져야 한다");

    rule.check(importLocalMainClasses);
  }
}
