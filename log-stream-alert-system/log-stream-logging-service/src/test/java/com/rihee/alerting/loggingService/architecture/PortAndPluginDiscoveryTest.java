package com.rihee.alerting.loggingService.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.architecture.support.ArchAssertions;
import com.rihee.alerting.loggingService.architecture.support.ArchitectureImports;
import com.rihee.alerting.loggingService.architecture.support.SpecFixtures;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.plugin.LogProcessorPlugin;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PortAndPluginDiscoveryTest {

  private static final JavaClasses CLASSES = ArchitectureImports.imports();

  @DisplayName("pipeline.port에 새 Port가 생기면 specs()에 반드시 등록되어야 한다")
  @Test
  void allPortTypesMustBeListedInSpecs() {
    // 발견
    Set<JavaClass> discoveredPorts = CLASSES.stream()
        .filter(c -> c.getPackageName().contains(".core.pipeline.port."))
        .filter(c -> c.getSimpleName().endsWith("Port"))
        .filter(c -> c.isAssignableTo(LogProcessorPort.class))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 기대
    Set<JavaClass> expectedPorts = SpecFixtures.specs()
        .map(PortSpec::portAbstracts).map(CLASSES::get)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 누락
    Set<JavaClass> missingPorts = new LinkedHashSet<>(discoveredPorts);
    missingPorts.removeAll(expectedPorts);

    String portHint = ArchAssertions.banner(
        "PortSpec 누락 감지!",
        ArchAssertions.simpleNames(discoveredPorts).toString(),
        ArchAssertions.simpleNames(expectedPorts).toString(),
        ArchAssertions.simpleNames(missingPorts).toString()
    );

    assertTrue(missingPorts.isEmpty(), portHint);
  }

  @DisplayName("core.plugin에 새 Plugin이 생기면 specs()에 반드시 등록되어야 한다")
  @Test
  void allPluginTypesMustBeListedInSpecs() {
    // 발견
    Set<JavaClass> discoveredPlugins = CLASSES.stream()
        .filter(c -> c.getPackageName().contains(".core.plugin"))
        .filter(c -> c.getSimpleName().endsWith("Plugin"))
        .filter(c -> c.isAssignableTo(LogProcessorPlugin.class))
        .filter(c -> !c.getName().equals(LogProcessorPlugin.class.getName()))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 기대
    Set<JavaClass> expectedPlugins = SpecFixtures.specs()
        .map(PortSpec::pluginClass).map(CLASSES::get)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    Set<JavaClass> missingPlugins = new LinkedHashSet<>(discoveredPlugins);
    missingPlugins.removeAll(expectedPlugins);

    String pluginHint = ArchAssertions.banner(
        "Plugin 누락 감지!",
        ArchAssertions.simpleNames(discoveredPlugins).toString(),
        ArchAssertions.simpleNames(expectedPlugins).toString(),
        ArchAssertions.simpleNames(missingPlugins).toString()
    );

    assertTrue(missingPlugins.isEmpty(), pluginHint);
  }
}
