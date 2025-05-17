package com.rihee.alerting.common.log.constant;

/**
 * 구조화된 로그의 meta 영역에 포함되는 표준 필드 키를 정의한 열거형입니다.
 *
 * <p>각 항목은 로그 메타 정보에 포함될 수 있는 핵심 필드명을 의미하며,
 * JSON 기반의 구조화 로그를 구성할 때 일관된 키 이름을 보장하기 위해 사용됩니다.</p>
 *
 * <p>예: 로그 전송 시 {@code "meta": {"method": "GET", "uri": "/api/test"}} 형태로 출력될 수 있습니다.
 *
 * @author 리희
 * @since 1.0
 */
public enum MetaProperties {
  /**
   * HTTP 요청 메서드 (예: GET, POST, PUT 등)를 나타냅니다.
   */
  METHOD("method"),
  /**
   * 요청된 URI 경로를 나타냅니다.
   */
  URI("uri"),
  /**
   * 응답 상태 코드 (예: 200, 404, 500 등)를 나타냅니다.
   */
  STATUS_CODE("statusCode"),
  /**
   * 응답 상태 메시지 (예: OK, NOT_FOUND 등)를 나타냅니다.
   */
  STATUS_MESSAGE("statusMessage"),
  /**
   * 요청-응답 간 소요 시간 (밀리초)을 나타냅니다.
   */
  ELAPSED_MS("elapsedMs");

  private final String key;

  MetaProperties(String key) {
    this.key = key;
  }

  /**
   * 로그 메타 필드의 키 문자열을 반환합니다.
   *
   * @return 구조화 로그에서 사용할 메타 키 이름
   */
  public String getKey() {
    return this.key;
  }
}
