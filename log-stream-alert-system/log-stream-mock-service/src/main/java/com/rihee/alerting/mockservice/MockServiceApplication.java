package com.rihee.alerting.mockservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * {@code MockServiceApplication}은 mock-service의 애플리케이션 진입점 클래스입니다.
 *
 * <p>{@link SpringBootApplication} 어노테이션을 통해 컴포넌트 스캔, 자동 설정, 설정 클래스 등록 등의
 * Spring Boot 부트스트랩 기능이 활성화되며, 독립 실행형 mock 서비스로 동작합니다.
 *
 * <p>본 mock 서비스는 로그 수집, 인터셉터 테스트, 모의 비즈니스 흐름 검증 등을 목적으로 구성되어 있으며,
 * 연계 테스트와 로깅 트레이싱 구조 검증에 사용됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@SpringBootApplication
@EnableWebSecurity
public class MockServiceApplication {

  /**
   * Spring Boot 애플리케이션을 실행하기 위한 진입 메서드입니다.
   *
   * @param args 커맨드라인 인자 (일반적으로 사용되지 않음)
   */
  public static void main(String[] args) {
    SpringApplication.run(MockServiceApplication.class, args);
  }

}
