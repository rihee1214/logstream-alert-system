package com.rihee.alerting.loggingService.test.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * RegistryEnumRulesTest 클래스는 ProcessorRegistryPaths enum의 이름이
 * 지정된 사양(spec)의 이름과 일치하는지 검증하는 테스트를 제공합니다.
 *
 * <p>이 클래스의 테스트는 JUnit 5와 ParameterizedTest를 기반으로 작성되었으며,
 * 외부에서 제공되는 PortSpec 데이터를 사용합니다.
 *
 * <p>주요 검증 내용: 각 spec 이름에 해당하는 enum 값이 ProcessorRegistryPaths에 정의되어 있는지 확인합니다.</p>
 *
 * <p>해당 테스트는 아키텍처의 일관성과 규칙 준수를 보장하기 위한 것입니다.
 *
 * @see PortSpec
 * @see com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures
 */
public class RegistryEnumRulesTest {

  /**
   * ProcessorRegistryPaths enum의 이름이 사양(spec) 이름과 일치하는지 검증하는 테스트 메서드입니다.
   * 각 spec 객체의 이름과 해당 RegistryPathEnum 클래스에 정의된 enum 값의 이름을 비교하여
   * 일치하는 값이 존재하는지 확인합니다.
   *
   * @param spec 검증 대상 PortSpec 객체로, 사양 이름과 RegistryPathEnum 클래스 정보를 포함합니다.
   */
  @DisplayName("ProcessorRegistryPaths enum 이름은 spec 이름과 일치해야 한다")
  @ParameterizedTest
  @MethodSource("com.rihee.alerting.loggingService.test.architecture.support.SpecFixtures#specs")
  void processorRegistryNameMustBeSpecName(PortSpec spec) {
    Class<? extends Enum<?>> enumClass = spec.registryPathEnum();

    boolean has = Stream.of(enumClass.getEnumConstants())
        .anyMatch(e -> e.name().equalsIgnoreCase(spec.name()));

    assertTrue(has,
        "spec 이름 `%s`에 대응되는 enum 상수가 `%s`에 존재하지 않습니다."
            .formatted(spec.name(), enumClass.getName()));
  }
}
