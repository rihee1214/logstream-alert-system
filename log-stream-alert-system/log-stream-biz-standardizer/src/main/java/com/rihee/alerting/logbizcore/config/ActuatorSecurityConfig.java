package com.rihee.alerting.logbizcore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

/**
 * {@code /actuator} 엔드포인트에 대한 접근 보안 정책을 설정하는 구성 클래스입니다.
 *
 * <p>다음과 같은 보안 규칙을 적용합니다:
 * <ul>
 *   <li>{@code /actuator/prometheus} 경로는 Prometheus 수집기에서만 접근할 수 있도록,
 *       미리 정의된 환경 변수 기반의 토큰 헤더 {@code X-Monitoring-Token} 값을 검증합니다.</li>
 *   <li>그 외의 모든 actuator 엔드포인트는 로컬호스트(127.0.0.1 또는 ::1)에서의 요청만 허용합니다.</li>
 * </ul>
 *
 * <p>이 설정은 운영 환경에서 actuator 정보가 외부에 노출되지 않도록 보안 관점에서의 기본 방어선을 제공합니다.
 *
 * @author 리희
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig {

  private static final String MONITORING_TOKEN_HEADER = "X-Monitoring-Token";

  private final String prometheusToken;

  private final String actuatorBaseUrl;

  /**
   * 환경 변수 또는 시스템 프로퍼티로부터 Prometheus 접근 토큰을 주입받아 초기화합니다.
   *
   * <p>이 토큰은 {@code /actuator/prometheus} 경로에 접근할 때,
   * 요청 헤더의 {@code X-Monitoring-Token} 값과 비교하여 인증을 수행하는 데 사용됩니다.</p>
   *
   * <p>설정 값은 {@code monitoring.token} 키를 통해 주입되며,
   * 값이 없거나 비어 있을 경우 애플리케이션 기동 시 예외가 발생합니다.</p>
   *
   * @param env Spring {@link Environment} 객체를 통해 외부 설정 값을 주입받습니다.
   * @throws IllegalStateException {@code monitoring.token} 설정이 존재하지 않거나 공백일 경우
   */
  public ActuatorSecurityConfig(Environment env) {
    this.prometheusToken = env.getProperty("monitoring.token");

    if (!StringUtils.hasText(this.prometheusToken)) {
      throw new IllegalStateException(
          "Missing required configuration: 'monitoring.token'. "
              + "Please set it using -Dmonitoring.token or environment variable."
      );
    }

    String actuatorBaseUrl = env.getProperty("management.endpoints.web.base-path");
    if (!StringUtils.hasText(actuatorBaseUrl)) {
      actuatorBaseUrl = "/actuator";
    }
    this.actuatorBaseUrl = actuatorBaseUrl;
  }

  /**
   * actuator 요청에 대한 보안 필터 체인을 정의합니다.
   *
   * <p>{@code /actuator/prometheus}는 Prometheus 전용 접근을 위해
   * {@code X-Monitoring-Token} 헤더 값을 검증하며, 기타 actuator 경로는 localhost 에서의 요청만 허용합니다.
   * </p>
   *
   * @param http Spring Security의 HttpSecurity 빌더
   * @return actuator 전용 {@link SecurityFilterChain}
   * @throws Exception 보안 설정 중 예외가 발생할 경우
   */
  @Bean
  @Order(0)
  public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
    http.securityMatcher(actuatorBaseUrl + "/**")
        .authorizeHttpRequests(auth
            -> auth
                    // prometheus 수집용 요청만 허용 (토큰 헤더 확인)
                    .requestMatchers(actuatorBaseUrl + "/prometheus")
                    .access((authSupplier, context) -> {
                      String token = context.getRequest().getHeader(MONITORING_TOKEN_HEADER);
                      return new AuthorizationDecision(prometheusToken.equals(token));
                    })
                    // /actuator/** 중 prometheus 외 모든 요청은 localhost 접근만 허용
                    .anyRequest()
                    .access((authentication, context)
                                                                    -> isLocalhostRequest(context))
        ).exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(401);
              response.setContentType("application/json");
              response.getWriter().write(
                  "{\"error\":\"Unauthorized\",\"message\":\"Access to actuator endpoint denied\"}"
              );
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(403);
              response.setContentType("application/json");
              response.getWriter().write(
                  "{\"error\":\"Forbidden\",\"message\":\"Insufficient permissions for actuator access\"}"
              );
            })
        )
        .csrf(CsrfConfigurer::disable);

    return http.build();
  }

  /**
   * 요청의 IP가 로컬호스트(127.0.0.1 또는 ::1)인지 확인하여 actuator 접근을 제한합니다.
   *
   * @param context 요청 컨텍스트
   * @return 로컬 요청이면 {@code true}, 아니면 {@code false}를 반환하는 {@link AuthorizationDecision}
   */
  private AuthorizationDecision isLocalhostRequest(RequestAuthorizationContext context) {
    String remoteAddr = context.getRequest().getRemoteAddr();
    return new AuthorizationDecision(
        "127.0.0.1".equals(remoteAddr) || "::1".equals(remoteAddr)
    );
  }
}
