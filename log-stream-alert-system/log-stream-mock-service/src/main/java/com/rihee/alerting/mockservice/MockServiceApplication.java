package com.rihee.alerting.mockservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@code MockServiceApplication}은 Mock 서비스의 진입점(entry point) 역할을 하는 Spring Boot 애플리케이션 클래스입니다.
 *
 * <p>이 클래스는 {@link SpringBootApplication} 어노테이션을 통해 자동 설정, 컴포넌트 스캔, Bean 등록 등의
 * 기본 설정을 활성화하며, 애플리케이션 실행 시 Spring Boot 환경을 부트스트랩합니다.
 *
 * <p>이 서비스는 테스트 및 연동 검증 목적의 mock endpoint를 제공하며, 독립적으로 실행 가능한 구조입니다.
 *
 * @author 리희
 * @since 1.0
 */
@SpringBootApplication
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
