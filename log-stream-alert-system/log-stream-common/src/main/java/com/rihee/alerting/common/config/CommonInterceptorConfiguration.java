package com.rihee.alerting.common.config;

import static com.rihee.alerting.common.constant.DefaultValues.LOGGING_DEFAULT_VALUE;

import com.rihee.alerting.common.interceptor.AbstractStructuredLogInterceptor;
import com.rihee.alerting.common.interceptor.DefaultStructuredLogInterceptor;
import com.rihee.alerting.common.interceptor.SpanLabelBeanPostProcessor;
import com.rihee.alerting.common.interceptor.SpanLabelRegistry;
import com.rihee.alerting.common.interceptor.StructuredLogInterceptorFactory;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC 설정 클래스입니다.
 *
 * <p>HTTP 요청 처리 흐름에 {@link DefaultStructuredLogInterceptor}를 등록하여,
 * 로그 트레이싱 및 MDC 컨텍스트 초기화/정리를 전역적으로 적용합니다.
 * </p>
 *
 * <p>또한
 * {@link SpanLabelRegistry}, {@link SpanLabelBeanPostProcessor},
 * {@link DefaultStructuredLogInterceptor} 등의 로깅 및 인터셉터 관련 빈을 명시적으로 구성하여 초기화 순서 및 의존성 문제를 방지합니다.
 * </p>
 *
 * @see DefaultStructuredLogInterceptor
 * @see SpanLabelRegistry
 * @see SpanLabelBeanPostProcessor
 */
@Configuration
@NonNullApi
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(prefix = "logging.interceptor", name = "enabled",
                        havingValue = "true", matchIfMissing = false)
public class CommonInterceptorConfiguration implements WebMvcConfigurer {

  /**
   * serviceName 로그에 포함될 서비스 이름(예: user-service).
   */
  private final String serviceName;

  /**
   * MDC 세팅을 위한 Interceptor에 사용될 serviceId 초기화.
   *
   * @param serviceName spanId 생성 규칙에 들어갈 서비스 명.
   */
  public CommonInterceptorConfiguration(@Value("${service.name:}") String serviceName) {
    this.serviceName = StringUtils.hasText(serviceName) ? serviceName
                                                      : LOGGING_DEFAULT_VALUE.getValue();
  }

  /**
   * {@link SpanLabelRegistry} 빈을 생성합니다.
   *
   * <p>각 컨트롤러 메서드에 선언된 {@code spanLabel} 정보를 등록/조회하기 위한 구조체로,
   * {@link SpanLabelBeanPostProcessor}에서 컨트롤러 메서드를 탐색하고
   * 해당 정보를 이 레지스트리에 저장합니다.
   * </p>
   *
   * @return {@link SpanLabelRegistry}의 인스턴스
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public SpanLabelRegistry spanLabelRegistry() {
    return new SpanLabelRegistry(); // 순수 POJO
  }

  /**
   * {@link SpanLabelBeanPostProcessor} 빈을 생성합니다.
   *
   * <p>애플리케이션 실행 시점에 모든 {@link com.rihee.alerting.common.annotation.StructuredRequestMapping}
   * 계열 어노테이션을 탐색하여, {@code spanLabel} 메타정보를 추출하고 {@link SpanLabelRegistry}에 등록합니다.
   * </p>
   *
   * <p>{@link org.springframework.beans.factory.config.BeanPostProcessor} 구현체이므로,
   * 반드시 수동으로 등록되어야 하며, Spring이 컨텍스트 초기화 단계에서 인식할 수 있도록 해야 합니다.
   * </p>
   *
   * @param registry {@link SpanLabelRegistry} 빈
   * @return {@link SpanLabelBeanPostProcessor} 인스턴스
   */
  // CGLIB으로 생성되는 프록시 이기 때문에 registry를 주입하기 위해서 먼저 생성되어야함
  @Bean
  public static SpanLabelBeanPostProcessor spanLabelBeanPostProcessor(SpanLabelRegistry registry) {
    return new SpanLabelBeanPostProcessor(registry);
  }

  /**
   * {@link AbstractStructuredLogInterceptor} 빈을 생성합니다.
   *
   * <p>이 인터셉터는 모든 HTTP 요청 전후에 MDC 값을 설정 및 해제하며,
   * 요청 간 traceId / parentSpanId / spanId의 추적을 가능하게 합니다.</p>
   *
   * <p>{@link StructuredLogInterceptorFactory}가 정의된 경우 이를 사용해
   * 커스텀 인터셉터를 생성하며, 그렇지 않으면 기본 {@link DefaultStructuredLogInterceptor}를 생성합니다.</p>
   *
   * @param registry spanLabel 정보를 보유한 {@link SpanLabelRegistry} 인스턴스
   * @param factoryProvider 커스텀 인터셉터 생성을 위한 팩토리의 지연 주입 제공자
   * @return MDC 설정용 {@link AbstractStructuredLogInterceptor} 인스턴스
   */
  @Bean
  public AbstractStructuredLogInterceptor structuredLogInterceptor(
                            SpanLabelRegistry registry,
                            ObjectProvider<StructuredLogInterceptorFactory> factoryProvider) {

    StructuredLogInterceptorFactory factory = factoryProvider.getIfAvailable();
    return factory != null ? factory.create(registry, serviceName)
                           : new DefaultStructuredLogInterceptor(registry, this.serviceName);
  }

  /**
   * {@link WebMvcConfigurer}를 구현한 빈을 통해
   * {@link AbstractStructuredLogInterceptor}를 모든 요청 경로에 등록합니다.
   *
   * <p>이 설정은 Spring MVC 핸들러 체인에 인터셉터를 전역적으로 포함시킵니다.</p>
   *
   * <p><b>우선순위:</b> {@code order(0)}으로 설정되어 있어 가장 먼저 실행됩니다.
   * 이는 traceId, spanId, logtype 등 로깅 기반 MDC 값 설정을 보장하기 위한 필수 설정입니다.</p>
   *
   * <p><b>주의:</b> 이후 등록되는 커스텀 인터셉터는 MDC 값을 덮어쓰거나 삭제하지 않도록 주의해야 합니다.
   * 순서 보장을 위해 {@code order > 0} 이상을 권장하며, 이 설정은 변경하지 않는 것이 좋습니다.</p>
   *
   * @param interceptor 등록할 MDC 로깅 인터셉터 인스턴스
   * @return 인터셉터가 등록된 {@link WebMvcConfigurer} 구현체
   */
  @Bean
  public WebMvcConfigurer addStructuredLogInterceptor(
                                                AbstractStructuredLogInterceptor interceptor) {
    return new WebMvcConfigurer() {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
          // 최우선 등록
          registry.addInterceptor(interceptor).order(0);
        }
    };
  }
}
