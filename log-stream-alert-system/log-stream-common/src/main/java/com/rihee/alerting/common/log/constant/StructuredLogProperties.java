package com.rihee.alerting.common.log.constant;

/**
 * {@code StructuredLogProperties}는
 * {@link com.rihee.alerting.common.interceptor.StructuredLogInterceptor}에서 사용되는
 * 구조화 로그 필드 키들을 정의하며, 로그 시스템 전반에서 통일된 속성 명명 규칙을 제공합니다.
 *
 * <p>모든 필드는 Micrometer, Brave, Zipkin 등과의 연동을 고려하여 B3 Header 표준과 일치하도록 설계되었으며,
 * 추후 외부 분산 추적 시스템과의 호환성을 보장합니다.
 *
 * <p>이 필드들은 MDC (Mapped Diagnostic Context)에 저장되며, 로그 백엔드(Kibana, Elasticsearch, Grafana 등)에서
 * 분석 가능하도록 JSON 기반 구조로 출력됩니다.
 */
public enum StructuredLogProperties {

  /**
   * 로그 타입.<br>
   * 업무 로그 : {@code biz}, 시스템 로그 : {@code sys}, 그 외 기본값 : {@code default}
   */
  LOG_TYPE("logtype"),

  /**
   * 로그 발생 시각.<br>
   * 포맷: ISO 8601 OffsetDateTime (예: 2025-04-27T15:16:15.641+0900)
   */
  TIME_STAMP("timestamp"),

  /**
   * 로그 레벨 (DEBUG, INFO, WARN, ERROR).
   */
  LEVEL("level"),

  /**
   * 로그를 발생시킨 마이크로서비스 또는 애플리케이션의 논리적 이름.
   */
  SERVICE("service"),

  /**
   * 전체 트랜잭션 흐름을 식별하는 유일한 ID.
   *
   * <p>해당 값은 B3 헤더의 {@code X-B3-TraceId}와 동일하게 설정되며,
   * 여러 마이크로서비스 간의 요청 흐름을 추적하는 데 사용됩니다.
   */
  TRACE_ID("traceId"),

  /**
   * 개별 작업 단위를 식별하는 ID.
   *
   * <p>B3 헤더의 {@code X-B3-SpanId}와 대응되며, 트랜잭션 내의 특정 작업을 구분하는 데 사용됩니다.
   */
  SPAN_ID("spanId"),

  /**
   * 상위 작업의 ID.
   *
   * <p>B3 헤더의 {@code X-B3-ParentSpanId}에 대응되며, 트리 구조 형태의 호출 관계를 표현합니다.
   */
  PARENT_SPAN_ID("parentSpanId"),

  /**
   * 로그 메시지 본문.
   */
  MESSAGE("message"),

  /**
   * 로그 발생 위치 클래스의 FQCN (Fully Qualified Class Name).
   */
  CLASS("class"),

  /**
   * 로그를 발생시킨 서버의 호스트 이름 (e.g., 서버 노드 구분용).
   */
  HOST("host"),

  /**
   * 로그를 발생시킨 컨테이너 이름 (e.g., pod 또는 docker 컨테이너 식별자).
   */
  CONTAINER("container"),

  /**
   * 예외 발생 시 stacktrace 출력 필드.
   *
   * <p>전체 예외 로그를 JSON 문자열로 포함합니다.
   */
  STACK_TRACE("stacktrace"),

  /**
   * 현재 로그 또는 트랜잭션 단위의 이름.<br>
   * 예: {@code purchaseProduct}, {@code cancelReservation}, {@code issueCoupon}
   *
   * <p>비즈니스 기능 또는 API 식별자 단위로 지정되며, 분산 추적 시 "span name" 역할을 수행합니다.
   */
  NAME("name"),

  /**
   * Zipkin 및 B3 헤더 호환용 trace sampling 여부.<br>
   * {@code 1} 또는 {@code 0} / {@code true} 또는 {@code false} 등의 값으로 표현됩니다.
   *
   * <p>B3 헤더: {@code X-B3-Sampled}
   */
  SAMPLED("sampled"),

  /**
   * Zipkin 및 B3 호환용 디버깅 수집 여부.<br>
   * 일반적으로 {@code 1} 또는 {@code 0}로 표현되며, 강제 수집 여부를 나타냅니다.
   *
   * <p>B3 헤더: {@code X-B3-Flags}
   */
  FLAGS("flags"),

  /**
   * 로그가 기록된 작업 또는 요청 처리의 소요 시간(ms 단위).
   *
   * <p>일반적으로 traceId 또는 spanId 단위의 duration을 측정하여 기록합니다.
   */
  DURATION("duration");

  private final String name;

  StructuredLogProperties(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
