package com.rihee.alerting.common.log.constant;

/**
 * {@code ReqProperties}는 구조화 로그 내 {@code req.} 접두어가 붙은 요청(Request) 관련 필드를 표준화하기 위해 정의된 열거형입니다.
 *
 * <p>각 항목은 HTTP 요청의 세부 정보를 나타내며, {@link org.slf4j.MDC}에 저장되어 로그로 출력됩니다.
 * 이는 meta 영역 제거 이후, 로그 필드를 명확하게 분리하고 분석 도구에서 쉽게 필터링 가능하도록 하기 위한 구조화 로깅 전략의 일환입니다.
 *
 * <p>대표적으로 요청 메서드({@code GET, POST 등}), URI, 응답 코드 및 메시지, 요청-응답 간 지연 시간(ms) 등을 포함합니다.
 *
 * <p>필드 이름은 모두 {@code req.} 접두어로 시작하며, 추후 {@code res.}, {@code user.}, {@code infra.} 등의 영역으로도 확장 가능합니다.
 *
 * @author 리희
 * @since 1.0
 */
public enum ReqProperties {
  /**
   * HTTP 요청 메서드 (예: GET, POST, PUT 등)를 나타냅니다.
   */
  METHOD("req.method"),
  /**
   * 요청된 URI 경로를 나타냅니다.
   */
  URI("req.uri"),
  /**
   * 응답 상태 코드 (예: 200, 404, 500 등)를 나타냅니다.
   */
  STATUS_CODE("req.statusCode"),
  /**
   * 응답 상태 메시지 (예: OK, NOT_FOUND 등)를 나타냅니다.
   */
  STATUS_MESSAGE("req.statusMessage"),
  /**
   * 요청-응답 간 소요 시간 (밀리초)을 나타냅니다.
   */
  ELAPSED_MS("req.elapsedMs");

  private final String key;

  ReqProperties(String key) {
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
