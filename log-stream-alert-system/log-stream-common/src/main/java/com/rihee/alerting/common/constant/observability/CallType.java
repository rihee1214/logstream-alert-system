package com.rihee.alerting.common.constant.observability;

/**
 * {@code CallType}은 시스템 간 통신이나 외부 자원 접근과 같은 호출(Invocation)의 유형을 정의하는 열거형입니다.
 *
 * <p>이 값은 구조화 로그에서 {@code call.type} 필드로 사용되며, 호출의 성격을 명확하게 구분하기 위한 기준으로 활용됩니다.
 * 현재는 HTTP 요청만을 나타내는 {@code HTTP} 타입만 정의되어 있으나,
 * 추후 Kafka, gRPC, DB, Redis 등 다양한 호출 유형이 추가될 수 있습니다.</p>
 *
 * <p>시스템 간 호출을 분류하고, 로그 분석, 추적, 오류 분리 등을 위한 핵심 메타데이터로 사용됩니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
public enum CallType {
  /**
   * HTTP 기반 호출을 나타냅니다.
   */
  HTTP("http");

  private final String type;

  CallType(String type) {
    this.type = type;
  }

  /**
   * 호출 유형에 대한 문자열 표현을 반환합니다.
   *
   * @return 예: "http"
   */
  public String getType() {
    return this.type;
  }
}
