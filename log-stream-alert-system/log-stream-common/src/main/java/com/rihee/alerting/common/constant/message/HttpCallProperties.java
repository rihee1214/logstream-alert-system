package com.rihee.alerting.common.constant.message;

/**
 * {@code CallProperties}는 구조화 로그에서 외부 시스템과의 통신(Call)에 관련된 필드들을 표준화하기 위한 열거형입니다.
 * 전체 스키마 설계 및 공통 정의에 대해서는
 * {@link com.rihee.alerting.common.constant.message package 설명}을 참고하세요.
 *
 * <p>{@code call.} 접두어를 사용하여 요청/응답 로그의 메타데이터를 일관되게 표현하며,
 * 다양한 프로토콜(HTTP, gRPC, Kafka 등)에 대해 공통된 구조로 로깅이 가능하도록 설계되었습니다.
 *
 * <p>호출 방식 구분은 {@code call.type} 필드를 통해 처리되며, 이 외에도 호출 메서드, 대상 URI,
 * 응답 상태 코드 및 메시지, 소요 시간(ms) 등을 포함합니다.
 *
 * <p>이 열거형은 {@link org.slf4j.MDC}를 통해 로그에 자동 포함되며,
 * 추적성과 분석 가능성을 높이기 위한 구조화 로깅 정책의 핵심 축입니다.
 *
 * @author 리희
 * @since 1.0
 */
public enum HttpCallProperties implements LogFieldKey {
  /** 호출 메서드 (예: GET, POST, PUBLISH 등). */
  METHOD("call.method"),

  /** 호출 대상 URI 또는 리소스 식별자. */
  URI("call.uri"),

  /** 응답 상태 코드 (예: 200, 404, 500 등). */
  STATUS_CODE("call.statusCode"),

  /** 응답 상태 메시지 (예: OK, NOT_FOUND 등). */
  STATUS_MESSAGE("call.statusMessage"),

  /** 응답에 포함된 상대방 시스템의 traceId (예: Zipkin 등에서 전달된 값). */
  RESP_TRACE_ID("call.remoteTraceId");

  private final String key;

  HttpCallProperties(String key) {
    this.key = key;
  }

  /**
   * 로그 메타 필드 명의 문자열을 반환합니다.
   *
   * @return 구조화 로그에서 사용할 메타 키 이름
   */
  @Override
  public String getFieldName() {
    return this.key;
  }
}
