package com.rihee.alerting.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 기반 공통 모듈의 시작점입니다.
 *
 * <p>이 모듈은 {@code @SpringBootApplication}으로 구성되어 있으며,
 * 애플리케이션의 실행 및 테스트 편의를 위한 진입점을 제공합니다.</p>
 *
 * <p>일반적으로 이 모듈은 단독 실행보다는 다른 서비스 모듈에서 공통 컴포넌트로 참조되며,
 * 로깅, 유틸리티, 공통 인터셉터, 메시지 처리 등의 기능을 제공합니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
@SpringBootApplication
public class CommonApplication {

  /**
   * Common 모듈의 Spring Boot 진입점입니다.
   * 테스트나 독립 실행용으로만 사용되며, 실제 운영 서비스는 이 모듈을 참조하여 구성합니다.
   *
   * @param args 프로그램 실행 인자
   */
  public static void main(String[] args) {
    SpringApplication.run(CommonApplication.class, args);
  }

}
