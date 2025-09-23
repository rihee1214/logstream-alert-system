package com.rihee.alerting.common.constant.observability;

/**
 * {@code CallCommonProperties}는 구조화 로그 내에서 호출(call)과 관련된
 * 공통 메타 데이터를 정의하는 열거형입니다.
 * 전체 스키마 설계 및 공통 정의에 대해서는
 * {@link com.rihee.alerting.common.constant.logging package 설명}을 참고하세요.
 *
 * <p>이 열거형에 포함된 필드는 호출 방식과 응답까지의 소요 시간 등,
 * HTTP, gRPC, Kafka 등 다양한 통신 방식에서 공통적으로 기록될 수 있는 항목들입니다.
 *
 * <p>각 필드는 {@code call.} 접두어로 시작하며, {@link org.slf4j.MDC}를 통해 로그에 포함되어
 * 추적 및 진단을 용이하게 합니다.
 *
 * <p>향후 통신 방식이 추가되더라도 이 enum은 호출 공통 필드를 표현하는 기준으로 유지됩니다.
 *
 * @author 리희
 * @since 1.0
 */
public enum CallCommonFields {

  /**
   * 호출 방식(type)을 나타냅니다.
   *
   * <p>예: {@code http}, {@code grpc}, {@code kafka} 등.
   * 이 값은 {@link CallType} 열거형을 통해 정의된 통신 유형 중 하나여야 합니다.
   */
  TYPE("call.type"),
  /**
   * 요청과 응답 사이의 전체 소요 시간(밀리초)을 나타냅니다.
   *
   * <p>성능 분석 및 병목 지점 확인을 위한 주요 지표로 활용됩니다.
   */
  ELAPSED_MS("call.elapsedMs");

  private final String key;

  CallCommonFields(String key) {
    this.key = key;
  }

  /**
   * 로그 메타 필드의 키 문자열을 반환합니다.
   *
   * @return 구조화 로그에서 사용할 메타 키 이름
   */
  public String getFieldName() {
    return this.key;
  }
}
