package com.rihee.alerting.mockservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@code MockServiceApplicationTests}는 mock-service 애플리케이션의 기본 구동 여부를 검증하기 위한 단위 테스트 클래스입니다.
 *
 * <p>Spring Boot 애플리케이션이 정상적으로 로딩되는지 확인하기 위해 {@code @SpringBootTest} 환경에서 실행되며,
 * CI 환경 및 로컬 개발 환경에서의 기본 기동 테스트로 활용됩니다.
 *
 * @author 리희
 */
@SpringBootTest
class MockServiceApplicationTests {

  /**
   * Spring Boot 애플리케이션 컨텍스트가 정상적으로 로딩되는지를 검증합니다.
   *
   * <p>구체적인 검증 로직은 없으며, 테스트 메서드 자체가 예외 없이 실행되면 테스트는 성공합니다.
   */
  @Test
  void contextLoads() {
  }

}
