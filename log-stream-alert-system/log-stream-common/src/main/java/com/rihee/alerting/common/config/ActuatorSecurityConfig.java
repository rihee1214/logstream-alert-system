package com.rihee.alerting.common.config;

import static com.rihee.alerting.common.constant.DefaultValues.PROMETHEUS_TOKEN_DEFAULT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

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
public class ActuatorSecurityConfig {

  private static final String MONITORING_TOKEN_HEADER = "X-Monitoring-Token";

  private final String prometheusToken;

  /**
   * 환경변수에서 Prometheus 접근 토큰을 불러와 내부 필드에 저장합니다.
   *
   * <p>이 토큰은 {@code /actuator/prometheus} 경로로 접근할 때,
   * 요청 헤더 {@code X-Monitoring-Token}과 비교하여 접근 여부를 판별하는 데 사용됩니다.</p>
   *
   * <p>환경변수 {@code monitoring.token}이 존재하지 않을 경우 기본값으로 {@code LOGGING_DEFAULT_VALUE}를 사용합니다.</p>
   *
   * @param env Spring {@link Environment} 객체를 통해 외부 설정 값을 주입받습니다.
   */
  public ActuatorSecurityConfig(Environment env) {
    this.prometheusToken = env.getProperty("monitoring.token", PROMETHEUS_TOKEN_DEFAULT.getValue());
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
  public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
    http.securityMatcher("/actuator/**")
        .authorizeHttpRequests(auth
            -> auth
                    // prometheus 수집용 요청만 허용 (토큰 헤더 확인)
                    .requestMatchers("/actuator/prometheus")
                    .access((authSupplier, context) -> {
                      String token = context.getRequest().getHeader(MONITORING_TOKEN_HEADER);
                      return new AuthorizationDecision(prometheusToken.equals(token));
                    })
                    // 나머지 actuator 요청은 localhost 에서만 허용
                    .anyRequest()
                    .access((authentication, context)
                        -> isLocalhostRequest(context)))
        .httpBasic(Customizer.withDefaults());

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
