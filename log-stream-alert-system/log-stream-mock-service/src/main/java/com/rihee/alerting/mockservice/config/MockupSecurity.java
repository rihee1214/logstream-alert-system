package com.rihee.alerting.mockservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * {@code MockupSecurity}는 mock-service의 보안 설정을 정의하는 Security 구성 클래스입니다.
 *
 * <p>이 클래스는 {@code mockup.token} 설정값과 HTTP 요청의 {@code X-Auth-Token} 헤더를 비교하여
 * 인증 여부를 판단하는 간단한 토큰 기반 인증 메커니즘을 제공합니다.
 *
 * <p>Spring Security의 {@link AuthorizationDecision}을 활용하여 토큰 값이 일치하는 경우에만
 * 요청을 허용하며, 일치하지 않는 경우 접근을 거부합니다.
 *
 * <p>이 설정은 테스트 목적보다는 실제 mock-service 운영 시에도 적용될 수 있으며,
 * {@link Order}를 통해 보안 필터 우선순위를 명시적으로 지정합니다.
 *
 * @author 리희
 */
@Configuration
public class MockupSecurity {

  /**
   * 인증 헤더에서 사용되는 토큰 이름 상수입니다.
   */
  private static final String MOCK_AUTH_TOKEN_HEADER = "X-Auth-Token";

  /**
   * application.properties 또는 환경 변수에서 주입되는 허용된 인증 토큰 값입니다.
   * 기본값은 "__UNDEFINED__"이며, 설정되지 않은 경우 인증이 실패하게 됩니다.
   */
  @Value("${mockup.token:__UNDEFINED__}")
  private String mockAuthToken;

  /**
   * 헤더 기반의 간단한 토큰 인증 필터 체인을 정의합니다.
   *
   * <p>모든 요청에 대해 {@code X-Auth-Token} 헤더를 검사하고, 그 값이 {@code mockup.token}
   * 설정 값과 정확히 일치할 경우 요청을 허용합니다. 일치하지 않으면 접근이 거부됩니다.
   *
   * <p>추가로 CSRF 보호는 테스트 및 간단한 통신 환경을 위해 비활성화되어 있습니다.
   *
   * @param http Spring Security HTTP 보안 설정 객체
   * @return 구성된 {@link SecurityFilterChain} 인스턴스
   * @throws Exception 설정 중 예외 발생 시
   */
  @Bean
  @Order(0)
  public SecurityFilterChain permitFilter(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth
        -> auth.anyRequest()
            .access((authSupplier, context) -> {
              String token = context.getRequest().getHeader(MOCK_AUTH_TOKEN_HEADER);
              return new AuthorizationDecision(mockAuthToken.equals(token));
            }))
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
