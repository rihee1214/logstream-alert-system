package com.rihee.alerting.common.config;

import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.TRACE_ID;

import com.rihee.alerting.common.interceptor.SpanLabelBeanPostProcessor;
import com.rihee.alerting.common.interceptor.SpanLabelRegistry;
import com.rihee.alerting.common.interceptor.StructuredLogInterceptor;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import io.micrometer.common.lang.NonNullApi;
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
 * {@code CommonInterceptorConfiguration}은 StructuredLogInterceptor를 Spring MVC에 등록하는 설정 클래스입니다.
 *
 * <p>해당 클래스는 모든 서비스에서 공통적으로 적용되는 structured logging interceptor를 Bean으로 등록하고,
 * Spring MVC의 InterceptorRegistry에 포함시킵니다.
 *
 * <p>설정은 자동 구성(auto configuration)으로 제공되며, 수동 구성 없이도 동작하도록 설계되어 있습니다.
 * 별도의 필터 체인 분리 없이, 모든 요청에 대해 structured logging이 자동 적용됩니다.
 *
 * @see StructuredLogInterceptor
 */
@Configuration
@NonNullApi
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(prefix = "logging.interceptor", name = "enabled",
                        havingValue = "true", matchIfMissing = false)
public class CommonInterceptorConfiguration implements WebMvcConfigurer {

  private static final StructuredLogger logger
                        = StructuredLoggerFactory.getLogger(CommonInterceptorConfiguration.class);
  /**
   * serviceName 로그에 포함될 서비스 이름(예: user-service).
   */
  private final String serviceName;

  private final int traceIdMultiplier;
  private final int spanIdMultiplier;

  /**
   * MDC 세팅을 위한 Interceptor에 사용될 serviceId 초기화.
   *
   * @param serviceName spanId 생성 규칙에 들어갈 서비스 명.
   */
  public CommonInterceptorConfiguration(@Value("${service.name}") String serviceName,
                                  @Value("${tracing.traceId.multiplier}") String traceIdMultiplier,
                                  @Value("${tracing.spanId.multiplier}") String spanIdMultiplier) {
    if (!StringUtils.hasText(serviceName)) {
      throw new IllegalStateException(
          "Missing required configuration: 'service.name'. "
              + "Please set it using -Dservice.name or environment variable."
      );
    }
    this.serviceName = serviceName;

    this.traceIdMultiplier = parseMultiplier(TRACE_ID.name(), traceIdMultiplier);
    this.spanIdMultiplier = parseMultiplier(SPAN_ID.name(), spanIdMultiplier);
  }

  private int parseMultiplier(String name, String value) {
    if (!StringUtils.hasText(value)) {
      return 1;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      logger.warn(LogType.SYS, "Invalid value for {}: '{}'. Using default value of 1.",
                                                                                      name, value);
      return 1;
    }
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
   * MDC 기반 structured logging을 위한 인터셉터 {@link StructuredLogInterceptor}를 생성합니다.
   *
   * <p>모든 HTTP 요청의 시작 시점에 traceId, spanId, parentSpanId, serviceName 등 로깅 관련 정보를
   * MDC에 자동 설정하며, 요청 종료 시점에는 MDC를 정리합니다.</p>
   *
   * <p>이 구현체는 시스템 정책에 따라 로그 추적 ID 생성 및 유효성 검사 정책을 고정화하며,
   * 커스터마이징 없이 통일된 방식으로 동작합니다.</p>
   *
   * @param registry spanLabel 정보를 보유한 {@link SpanLabelRegistry} 인스턴스
   * @return MDC 설정용 {@link StructuredLogInterceptor} 인스턴스
   */
  @Bean
  public StructuredLogInterceptor structuredLogInterceptor(
                            SpanLabelRegistry registry) {
    return new StructuredLogInterceptor(registry,
                                        this.traceIdMultiplier, this.spanIdMultiplier);
  }

  /**
   * {@link StructuredLogInterceptor}를 Spring MVC의 전역 인터셉터로 등록합니다.
   *
   * <p>이 설정은 {@code order(0)}으로 가장 먼저 실행되도록 보장되며,
   * 로그 추적에 필요한 MDC 필드(traceId, spanId, serviceName 등)를 요청 초기에 설정합니다.</p>
   *
   * <p><b>정책 강제화:</b> 해당 인터셉터는 사용자 정의 로직 없이 정책에 따라 고정된 방식으로 동작하며,
   * 시스템의 모든 서비스에서 동일한 포맷의 로그를 강제합니다.</p>
   *
   * <p><b>주의사항:</b> 이후에 등록되는 모든 커스텀 인터셉터는 MDC 값을 수정하거나 제거하지 않도록 주의해야 합니다.
   * 반드시 {@code order > 0} 이상의 값으로 등록하십시오.</p>
   *
   * @param interceptor 등록할 MDC 로깅 인터셉터 인스턴스
   * @return 인터셉터가 등록된 {@link WebMvcConfigurer} 구현체
   */
  @Bean
  public WebMvcConfigurer addStructuredLogInterceptor(
                                                StructuredLogInterceptor interceptor) {
    return new WebMvcConfigurer() {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
          registry.addInterceptor(interceptor).order(0);
        }
    };
  }
}
