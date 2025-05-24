package com.rihee.alerting.mockservice.constants;

/**
 * {@code MockupHeaders}는 mock-service 전용 HTTP 헤더 이름을 정의하는 열거형입니다.
 *
 * <p>테스트용 mock 서비스에서 사용하는 커스텀 HTTP 헤더 값을 상수화하여,
 * 하드코딩 없이 안전하고 일관된 방식으로 헤더를 참조할 수 있도록 돕습니다.
 *
 * <p>예를 들어 WebClient 또는 RestTemplate 사용 시 다음과 같이 활용됩니다:
 * <pre>{@code
 * httpHeaders.set(MockupHeaders.MOCK_AUTH_TOKEN_HEADER.getHeaderName(), "test-token");
 * }</pre>
 *
 * <p>이 enum은 추후 mock 제어용 헤더가 추가될 경우 확장될 수 있도록 설계되었습니다.
 *
 * @author 리희
 * @since 1.0
 */
public enum MockupHeaders {
  /**
   * mock 요청에서 사용되는 인증 토큰 헤더.
   *
   * <p>mock 서비스는 이 헤더를 이용해 테스트 시나리오를 구분하거나,
   * 요청의 유효성을 식별할 수 있습니다.
   *
   * <p>헤더 이름: {@code "X-Auth-Token"}
   */
  MOCK_AUTH_TOKEN_HEADER("X-Auth-Token");

  /**
   * HTTP 요청에 사용될 실제 헤더 이름 문자열입니다.
   */
  private final String headerName;

  MockupHeaders(String headerName) {
    this.headerName = headerName;
  }

  /**
   * 헤더 이름 문자열을 반환합니다.
   *
   * @return HTTP 요청에 사용될 헤더 이름
   */
  public String getHeaderName() {
    return this.headerName;
  }
}
