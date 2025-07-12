package com.rihee.alerting.loggingService.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ScyllaDB 접속 설정을 정의하는 설정 클래스입니다.
 *
 * <p>이 클래스는 Spring의 {@code @Configuration} 애노테이션을 통해
 * CqlSession을 빈으로 등록하며, ScyllaDB와의 연결을 위한 핵심 구성을 제공합니다.
 *
 * <p>구체적인 접속 정보(예: contact points, datacenter 등)는 {@code application.properties}
 * 또는 {@code application.yml} 파일에 정의되어 있어야 합니다.
 *
 * @author 리희
 * @since 1.0
 */
@Configuration
public class ScyllaConfig {

  /**
   * ScyllaDB에 연결하기 위한 {@link CqlSession} 빈을 생성합니다.
   *
   * <p>이 세션 객체는 DAO 또는 Repository 계층에서 ScyllaDB 쿼리를 실행하기 위한 핵심 객체이며,
   * 설정 파일에 명시된 접속 정보를 기반으로 클러스터에 연결합니다.
   *
   * @return ScyllaDB 연결을 위한 {@code CqlSession} 객체
   */
  @Bean
  public CqlSession cqlSession() {

    return CqlSession.builder()

            .build();
  }
}
