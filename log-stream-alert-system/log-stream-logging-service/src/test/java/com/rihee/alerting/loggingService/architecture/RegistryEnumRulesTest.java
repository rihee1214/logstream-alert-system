package com.rihee.alerting.loggingService.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.architecture.constants.PortSpec;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RegistryEnumRulesTest {

  @DisplayName("ProcessorRegistryPaths enum 이름은 spec 이름과 일치해야 한다")
  @ParameterizedTest
  @MethodSource("com.rihee.alerting.loggingService.architecture.support.SpecFixtures#specs")
  void processorRegistryNameMustBeSpecName(PortSpec spec) {
    Class<? extends Enum<?>> enumClass = spec.registryPathEnum();

    boolean has = Stream.of(enumClass.getEnumConstants())
        .anyMatch(e -> e.name().equalsIgnoreCase(spec.name()));

    assertTrue(has,
        "spec 이름 `%s`에 대응되는 enum 상수가 `%s`에 존재하지 않습니다."
            .formatted(spec.name(), enumClass.getName()));
  }
}
