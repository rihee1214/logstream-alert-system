package com.rihee.alerting.common.constant.logging;

import com.rihee.alerting.common.constant.annotation.LogPolicy;

/**
 * {@code StructuredLogProperties}는
 * 구조화 로그 필드 키들을 정의하며, 로그 시스템 전반에서 통일된 속성 명명 규칙을 제공합니다.
 * 전체 스키마 설계 및 공통 정의에 대해서는
 * {@link com.rihee.alerting.common.constant.logging package 설명}을 참고하세요.
 *
 * <p>모든 필드는 Micrometer, Brave, Zipkin 등과의 연동을 고려하여 B3 Header 표준과 일치하도록 설계되었으며,
 * 추후 외부 분산 추적 시스템과의 호환성을 보장합니다.
 *
 * <p>이 필드들은 MDC (Mapped Diagnostic Context)에 저장되며, 로그 백엔드(Kibana, Elasticsearch, Grafana 등)에서
 * 분석 가능하도록 JSON 기반 구조로 출력됩니다.
 */
public enum StructuredLogFields {

  LOG_MESSAGE_ID("logMessageId"),
  /**
   * 로그 타입.<br>
   * 업무 로그 : {@code biz}, 시스템 로그 : {@code sys}, 그 외 기본값 : {@code default}
   */
  LOG_TYPE("logType"),

  /**
   * 로그 발생 시각.<br>
   * 포맷: ISO 8601 OffsetDateTime (예: 2025-04-27T15:16:15.641+0900)
   */
  @LogPolicy(isEssential = true, description = "로그의 발생 시각을 나타냅니다. (포맷 : ISO 8601)")
  TIME_STAMP("timestamp"),

  /**
   * 로그 레벨 (DEBUG, INFO, WARN, ERROR).
   */
  @LogPolicy(isEssential = true, description = "알림, 필터링 정책의 기준이 되는 필드입니다.")
  LOG_LEVEL("logLevel"),

  /**
   * 로그를 발생시킨 마이크로서비스 또는 애플리케이션의 논리적 이름.
   */
  @LogPolicy(isEssential = true, description = "로그를 발생시킨 마이크로서비스 또는 애플리케이션의 논리적 이름입니다.")
  SERVICE_NAME("serviceName"),

  /**
   * 전체 트랜잭션 흐름을 식별하는 유일한 ID.
   *
   * <p>해당 값은 B3 헤더의 {@code X-B3-TraceId}와 동일하게 설정되며,
   * 여러 마이크로서비스 간의 요청 흐름을 추적하는 데 사용됩니다.
   */
  @LogPolicy(isEssential = true, description = "전체 트랜잭션 흐름을 식별하는 유일한 ID 입니다.")
  TRACE_ID("traceId"),

  /**
   * 개별 작업 단위를 식별하는 ID.
   *
   * <p>B3 헤더의 {@code X-B3-SpanId}와 대응되며, 트랜잭션 내의 특정 작업을 구분하는 데 사용됩니다.
   */
  @LogPolicy(isEssential = true, description = "개별 작업 단위를 식별하는 ID 입니다.")
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
  @LogPolicy(isEssential = true, description = "로그 발생 위치 클래스의 FQCN 입니다.")
  CLASS_NAME("className"),

  /**
   * 로그를 발생시킨 서버의 호스트 이름 (e.g., 서버 노드 구분용).
   */
  @LogPolicy(isEssential = true, description = "로그를 발생시킨 서버의 호스트 이름입니다.")
  HOST("host"),

  /**
   * 로그를 발생시킨 컨테이너 이름 (e.g., pod 또는 docker 컨테이너 식별자).
   */
  @LogPolicy(isEssential = true, description = "로그를 발생시킨 컨테이너 이름입니다.")
  CONTAINER("container"),

  /**
   * 예외 발생 시 stacktrace 출력 필드.
   *
   * <p>전체 예외 로그를 JSON 문자열로 포함합니다.
   */
  STACKTRACE("stacktrace"),

  /**
   * 현재 로그 또는 트랜잭션 단위의 이름.<br>
   * 예: {@code purchaseProduct}, {@code cancelReservation}, {@code issueCoupon}
   *
   * <p>비즈니스 기능 또는 API 식별자 단위로 지정되며, 분산 추적 시 "span name" 역할을 수행합니다.
   */
  @LogPolicy(isEssential = true, description = "현재 로그 또는 트랜잭션 단위의 이름입니다.")
  NAME("name");

  private final String name;

  StructuredLogFields(String name) {
    this.name = name;
  }

  public String getFieldName() {
    return this.name;
  }
}
