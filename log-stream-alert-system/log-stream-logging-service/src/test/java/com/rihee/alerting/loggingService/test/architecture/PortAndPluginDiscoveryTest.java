package com.rihee.alerting.loggingService.test.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.plugin.LogProcessorPlugin;
import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.test.architecture.support.ArchAssertions;
import com.rihee.alerting.loggingService.test.architecture.support.ArchitectureImports;
import com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Port와 Plugin의 발견 및 등록 여부를 검증하는 테스트 클래스입니다.
 *
 * <p>이 클래스는 `pipeline.port` 및 `core.plugin` 패키지에서 새롭게 추가된 Port와 Plugin이
 * 사전 정의된 명세(spec)에 포함되어 있는지를 확인하여, 누락된 항목이 없는지 검증합니다.
 * 이를 통해 Port와 Plugin의 관리와 일관성을 유지하도록 돕습니다.
 */
public class PortAndPluginDiscoveryTest {

  /**
   * 테스트 대상 JavaClasses를 import하는 공통 지원 클래스.
   */
  private static final JavaClasses CLASSES = ArchitectureImports.imports();

  /**
   * 새롭게 추가된 Port 타입이 모두 명세(specs)에 포함되어 있는지 검증하는 테스트입니다.
   *
   * <p>이 테스트는 pipeline.port 패키지 내에 정의된 모든 Port 타입을 탐지한 후,
   * 사전에 정의된 명세(SpecFixtures.specs() 메서드에서 제공되는 PortSpec 객체들)에
   * 해당 Port들이 올바르게 등록되어 있는지 확인합니다.
   *
   * <p>Port가 누락된 경우, 누락된 Port의 목록과 함께 테스트가 실패하며, 이를 통해
   * Port 추가 시 명세 등록을 강제할 수 있습니다.
   *
   * <p>주요 검증 과정:
   * <ol>
   *  <li>pipeline.port 패키지에서 `LogProcessorPort` 인터페이스를 구현한 모든 Port를 검색.</li>
   *  <li>명세(SpecFixtures.specs())에 등록된 Port와 비교.</li>
   *  <li>발견된 Port 중 명세에 누락된 Port가 있는지 확인.</li>
   * </ol>
   * </p>
   *
   * <p>테스트 실패 시 제공되는 정보:
   * <ul>
   *   <li>발견된 포트</li>
   *   <li>명세에 등록된 포트</li>
   *   <li>누락된 포트</li>
   * </ul>
   * </p>
   *
   * <p><b>이러한 검증을 통해 Port 추가 시 누락을 방지하고 시스템의 일관성을 유지할 수 있습니다.</b></p>
   *
   * <p><b>Assertion</b>: 발견된 Port와 명세에 포함된 Port가 완전히 일치해야 합니다.</p>
   */
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

  /**
   * core.plugin 패키지 내 새롭게 추가된 Plugin 타입이 사전에 정의된 명세(specs)에 반드시 등록되어 있는지 검증하는 테스트입니다.
   * 이 테스트는 시스템 내 플러그인이 올바르게 정의 및 관리될 수 있도록 보장합니다.
   *
   * <p>주요 검증 과정:
   * <ol>
   *   <li>core.plugin 패키지 내에서 LogProcessorPlugin을 구현한 모든 클래스(Plugin)를 검색합니다.</li>
   *   <li>SpecFixtures.specs() 메서드에서 제공되는 Plugin 명세와 비교하여 등록 여부를 확인합니다.</li>
   *   <li>명세에 누락된 Plugin이 있을 경우 이를 알려줍니다.</li>
   * </ol>
   * </p>
   *
   * <p>테스트 실패 시 제공되는 정보:
   * <ul>
   *   <li>발견된 Plugin 목록</li>
   *   <li>명세에 등록된 Plugin 목록</li>
   *   <li>누락된 Plugin 목록</li>
   * </ul>
   * </p>
   *
   * <p><b>Assertion</b>: 발견된 Plugin과 명세에 포함된 Plugin이 완전히 일치해야 합니다.</p>
   */
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
