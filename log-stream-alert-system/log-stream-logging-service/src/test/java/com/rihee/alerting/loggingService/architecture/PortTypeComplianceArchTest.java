package com.rihee.alerting.loggingService.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.plugin.LogProcessorPlugin;
import com.rihee.alerting.loggingService.toos.annotationprocessor.PersistenceTypeProcessor;
import com.rihee.alerting.loggingService.toos.constants.ProcessorRegistryPaths;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
            com.rihee.alerting.loggingService.core.plugin.LogCollectorPlugin.class,
            com.rihee.alerting.loggingService.annotations.CollectorType.class,
            com.rihee.alerting.loggingService.toos.annotationprocessor.CollectorTypeProcessor.class,
            ProcessorRegistryPaths.COLLECTOR.getDeclaringClass()
        ),
        new PortSpec(
            "validator",
            com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort.class,
            "com.rihee.alerting.loggingService.adapter.rule.validator..",
            com.rihee.alerting.loggingService.core.plugin.LogValidatorPlugin.class,
            com.rihee.alerting.loggingService.annotations.ValidatorType.class,
            com.rihee.alerting.loggingService.toos.annotationprocessor.ValidatorTypeProcessor.class,
            ProcessorRegistryPaths.VALIDATOR.getDeclaringClass()
        ),
        new PortSpec(
            "persistence",
            com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort.class,
            "com.rihee.alerting.loggingService.adapter.out.persistence..",
            com.rihee.alerting.loggingService.core.plugin.LogPersistencePlugin.class,
            com.rihee.alerting.loggingService.annotations.PersistenceType.class,
            PersistenceTypeProcessor.class,
            ProcessorRegistryPaths.PERSISTENCE.getDeclaringClass()
        )
    );
  }

  @DisplayName("pipeline.port에 새 Port가 생겼다면 specs()에 반드시 등록되어야 한다")
  @Test
  void allPortTypesInCodeMustBeListedInSpecs() {
    // 발견된 Port(JavaClass)
    Set<JavaClass> discoveredPorts = IMPORT_LOCAL_MAIN_CLASSES.stream()
        .filter(c -> c.getPackageName().contains(".core.pipeline.port."))
        .filter(c -> c.getSimpleName().endsWith("Port"))
        .filter(c -> c.isAssignableTo(LogProcessorPort.class))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // === specs()에 정의된 예상 포트 클래스 목록 ===
    Set<JavaClass> expectedPorts = specs()
        .map(PortSpec::portInterface)
        .map(IMPORT_LOCAL_MAIN_CLASSES::get) // Class<?> → JavaClass
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 발견된 Plugin(JavaClass)
    Set<JavaClass> discoveredPlugins = IMPORT_LOCAL_MAIN_CLASSES.stream()
        .filter(c -> c.getPackageName().contains(".core.plugin"))
        .filter(c -> c.getSimpleName().endsWith("Plugin"))
        .filter(c -> c.isAssignableTo(LogProcessorPlugin.class))
        .filter(c -> !c.getName().equals(LogProcessorPlugin.class.getName()))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // === specs()에 정의된 예상 플러그인 클래스 목록 ===
    Set<JavaClass> expectedPlugins = specs()
        .map(PortSpec::pluginClass)
        .map(IMPORT_LOCAL_MAIN_CLASSES::get)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // === 누락된 Port/Plugin 계산 ===
    Set<JavaClass> missingPorts = new LinkedHashSet<>(discoveredPorts);
    missingPorts.removeAll(expectedPorts);

    Set<JavaClass> missingPlugins = new LinkedHashSet<>(discoveredPlugins);
    missingPlugins.removeAll(expectedPlugins);

    // === 진단 메시지 생성 ===
    String portHint = """
        ❌ PortSpec 누락 감지!
        발견된 Port 목록       : %s
        specs() 등록된 Port 목록: %s
        누락된 Port 목록       : %s
        """.formatted(
        toSimpleNames(discoveredPorts),
        toSimpleNames(expectedPorts),
        toSimpleNames(missingPorts)
    );

    String pluginHint = """
        ❌ Plugin 누락 감지!
        발견된 Plugin 목록       : %s
        specs() 등록된 Plugin 목록: %s
        누락된 Plugin 목록       : %s
        """.formatted(
        toSimpleNames(discoveredPlugins),
        toSimpleNames(expectedPlugins),
        toSimpleNames(missingPlugins)
    );

    // === 실패 처리 ===
    assertTrue(missingPorts.isEmpty(), portHint);
    assertTrue(missingPlugins.isEmpty(), pluginHint);
  }

  private static List<String> toSimpleNames(Set<JavaClass> classes) {
    return classes.stream().map(JavaClass::getSimpleName).toList();
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
