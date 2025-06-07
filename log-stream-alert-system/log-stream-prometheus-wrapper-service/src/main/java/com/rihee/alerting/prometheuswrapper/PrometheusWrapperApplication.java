package com.rihee.alerting.prometheuswrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Prometheus Wrapper Spring Boot 애플리케이션의 진입점 클래스입니다.
 *
 * <p>이 애플리케이션은 Prometheus로부터 전달되는 알림을 수신하여,
 * 사용자 정의 정책을 기반으로 AlertManager 또는 대체 NotificationService로 전달하는 중계 역할을 수행합니다.
 *
 * <p>주로 사이드카 또는 게이트웨이 형태로 함께 배포되며, 복잡한 알림 정책 처리 및
 * AlertManager 전송 실패 시의 대응 로직을 선제적으로 수행하도록 설계되어 있습니다.
 *
 * <p>알림 파이프라인의 유연성과 견고함을 높이기 위한 보조 컴포넌트로 사용됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@SpringBootApplication
public class PrometheusWrapperApplication {

  /**
   * PrometheusWrapper 애플리케이션의 진입점입니다.
   *
   * <p>Spring Boot 기반으로 애플리케이션 컨텍스트를 초기화하고,
   * 내장 서버(Netty)를 시작하여 HTTP 요청을 처리할 준비를 합니다.
   * 일반적으로 이 메서드는 Docker 또는 Kubernetes 환경에서 컨테이너가 시작될 때 호출됩니다.
   *
   * @param args 커맨드라인 인자로 전달되는 설정값
   */
  public static void main(String[] args) {
    SpringApplication.run(PrometheusWrapperApplication.class, args);
  }

}
