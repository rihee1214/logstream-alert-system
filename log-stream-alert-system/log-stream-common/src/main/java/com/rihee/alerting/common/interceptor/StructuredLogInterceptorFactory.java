package com.rihee.alerting.common.interceptor;

/**
 * {@code StructuredLogInterceptorFactory}는 서비스별 커스텀 {@link AbstractStructuredLogInterceptor}
 * 인스턴스를 생성하기 위한 함수형 인터페이스입니다.
 *
 * <p>이 인터페이스를 구현하면 공통 모듈의 기본 인터셉터를 대체하여,
 * traceId, spanId 생성 전략을 사용자 정의 방식으로 커스터마이징할 수 있습니다.</p>
 *
 * <p>예를 들어 서비스 이름 또는 메서드 라벨에 따라 spanId를 생성하는 규칙을 재정의할 수 있으며,
 * 공통 모듈에서 {@code ConditionalOnMissingBean}을 통해 자동으로 선택됩니다.</p>
 *
 * <p><b>사용 예시:</b>
 * <pre>{@code
 * @Bean
 * public StructuredLogInterceptorFactory customFactory() {
 *     return (registry, serviceName) -> new MyCustomInterceptor(registry, serviceName);
 * }
 * }</pre>
 *
 * @see AbstractStructuredLogInterceptor
 * @see DefaultStructuredLogInterceptor
 */
@FunctionalInterface
public interface StructuredLogInterceptorFactory {

  /**
   * 커스텀 {@link AbstractStructuredLogInterceptor} 인스턴스를 생성합니다.
   *
   * @param registry 컨트롤러 메서드에 설정된 spanLabel을 저장한 레지스트리
   * @param serviceName 현재 서비스 이름 (spanId 생성에 사용됨)
   * @return 생성된 StructuredLogInterceptor 인스턴스
   */
  AbstractStructuredLogInterceptor create(SpanLabelRegistry registry, String serviceName);
}
