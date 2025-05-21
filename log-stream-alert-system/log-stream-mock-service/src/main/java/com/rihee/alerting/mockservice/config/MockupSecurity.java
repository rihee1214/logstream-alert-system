package com.rihee.alerting.mockservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

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
   * 외부에서 주입되는 인증 토큰 값으로, mock-service에 대한 요청 시
   * HTTP 헤더에 포함되어야 하는 값입니다.
   *
   * <p>해당 값은 {@code application.properties}, 시스템 프로퍼티({@code -Dmockup.token}),
   * 또는 환경 변수({@code MOCKUP_TOKEN})를 통해 주입됩니다.
   *
   * <p>설정 값이 없을 경우, {@code MockupSecurity} Bean 생성 시점에 예외가 발생하며
   * 애플리케이션이 기동되지 않습니다.
   *
   * <p>또한 요청 헤더에 이 값이 포함되지 않으면 인증이 실패합니다.
   */
  private final String mockAuthToken;

  /**
   * {@code MockupSecurity}는 mock-service 전용 보안 필터 구성을 담당하는 클래스입니다.
   *
   * <p>해당 클래스는 외부에서 주입된 {@code mockup.token} 값을 기반으로,
   * 인증 토큰 검증 필터의 기준값을 초기화합니다.
   * 이 값은 서비스 외부 호출 시 헤더에 포함되어야 하며,
   * 설정되지 않을 경우 애플리케이션 기동이 실패하도록 구성되어 있습니다.
   *
   * <p>{@code mockup.token}은 시스템 프로퍼티({@code -Dmockup.token})나
   * 환경 변수({@code MOCKUP_TOKEN}), {@code application.properties}를 통해 주입되어야 하며,
   * 내부에서 유효성 검증을 통해 공백 또는 누락된 경우 예외를 발생시킵니다.
   *
   * @param mockAuthToken mock 서비스 호출 시 헤더로 전달되어야 할 인증 토큰
   * @throws IllegalStateException 설정 값이 비어 있거나 누락된 경우 기동 실패
   */
  public MockupSecurity(@Value("${mockup.token}") String mockAuthToken) {
    if (!StringUtils.hasText(mockAuthToken)) {
      throw new IllegalStateException(
          "Missing required configuration: 'mockup.token'. "
              + "Please set it using -Dmockup.token or environment variable."
      );
    }
    this.mockAuthToken = mockAuthToken;
  }

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
