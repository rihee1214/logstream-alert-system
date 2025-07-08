package com.rihee.alerting.logbizcore.configuration;

import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 통합 테스트 환경에서 {@link HttpServletRequest}를 모킹하여 주입하기 위한 테스트 전용 구성 클래스입니다.
 *
 * <p>{@code @SpringBootTest} 또는 {@code @WebMvcTest} 등의 테스트 환경에서
 * 의존성 주입이 필요한 경우, 실제 서블릿 요청 객체 대신 Mockito로 생성된 {@code HttpServletRequest} mock 객체를 제공합니다.</p>
 *
 * <p>해당 설정은 주로 AOP, Interceptor, 또는 필터 등에서 {@code HttpServletRequest}에 직접 접근할 때
 * 테스트 환경에서의 NullPointerException을 방지하고자 사용됩니다.</p>
 *
 * <p>{@code @Primary} 어노테이션을 사용하여 다른 {@code HttpServletRequest} 빈 정의보다 우선적으로 주입되도록 설정되어 있습니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
@TestConfiguration
public class MockHttpServletRequestConfig {

  /**
   * 테스트 전용 {@link HttpServletRequest} 모킹 객체를 생성하여 빈으로 등록합니다.
   *
   * <p>이 빈은 {@code @Primary}로 지정되어 있어 테스트 환경에서 사용되는 {@link HttpServletRequest}
   * 의존성에 우선적으로 주입됩니다.</p>
   *
   * @return 모킹된 {@code HttpServletRequest} 인스턴스
   */
  @Bean
  @Primary
  public HttpServletRequest httpServletRequest() {
    return mock(HttpServletRequest.class);
  }
}
