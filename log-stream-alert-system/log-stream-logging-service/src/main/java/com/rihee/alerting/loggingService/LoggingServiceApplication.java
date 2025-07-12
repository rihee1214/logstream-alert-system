package com.rihee.alerting.loggingService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Logging Service 애플리케이션의 진입점 클래스입니다.
 *
 * <p>이 모듈은 외부에서부터 수신한 로그 메시지를 ScyllaDB에 저장하고,
 * 후속 처리나 알림 시스템으로의 전달을 위한 기반 구조를 제공합니다.
 *
 * <p>또한, 로그 데이터의 백업 및 장기 보관 전략의 시작점으로서
 * 추후 아카이빙, 분석, 알림 트리거 등의 연계 처리를 위한 핵심 역할을 수행합니다.
 *
 * <p>Spring Boot 기반의 애플리케이션으로 동작하며,
 * {@code @SpringBootApplication} 애노테이션을 통해 컴포넌트 스캔, 자동 설정 등을 활성화합니다.
 *
 * @author 리희
 * @since 1.0
 */
@SpringBootApplication
public class LoggingServiceApplication {

  /**
   * Logging Service 애플리케이션을 실행하는 메인 메서드입니다.
   *
   * <p>Spring Boot의 {@link SpringApplication#run(Class, String...)} 메서드를 호출하여
   * 전체 애플리케이션을 부트스트랩합니다.
   *
   * @param args 커맨드라인 인수
   */
  public static void main(String[] args) {
    SpringApplication.run(LoggingServiceApplication.class, args);
  }

}
