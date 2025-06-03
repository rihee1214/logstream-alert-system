package com.rihee.alerting.common.util.client.web.response;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse.Headers;

/**
 * {@code WebClientCallResult}는 WebClient 호출의 결과를 구조화된 형식으로 나타내는 래퍼 클래스입니다.
 *
 * <p>호출 성공 여부, 응답 상태 코드 및 헤더, 실제 응답 본문 데이터, 호출 소요 시간(ms) 등의 정보를 포함하며,
 * WebClient 호출의 추적 및 로깅을 위해 활용됩니다.
 *
 * @param <T> 응답 본문의 타입(되도록 String 타입 사용)
 * @author 리희
 * @since 1.0
 */
public class WebClientCallResult<T> {

  /** 응답 메타 정보 및 본문을 담고 있는 내부 객체. */
  private final HttpClientResponse<T> response;

  /** WebClient 호출의 총 소요 시간 (단위: 밀리초). */
  private final long elapsedMillis;

  /**
   * 내부 생성자. {@link #processedWebClientCallResult}를 통해 인스턴스를 생성하세요.
   *
   * @param response 응답 상태 및 데이터
   * @param elapsedMillis 호출 소요 시간
   */
  private WebClientCallResult(HttpClientResponse<T> response, long elapsedMillis) {
    this.response = response;
    this.elapsedMillis = elapsedMillis;
  }

  /**
   * WebClient 호출 결과를 감싸는 {@code WebClientCallResult}를 생성합니다.
   *
   * @param status       HTTP 상태 코드
   * @param headers      응답 헤더
   * @param data         응답 본문 (JSON 문자열 등)
   * @param elapsedMillis 호출 소요 시간 (단위: 밀리초)
   * @param <T>          응답 데이터 타입 (일반적으로 {@code String} 사용)
   * @return 구조화된 WebClient 호출 결과
   */
  public static <T> WebClientCallResult<T> processedWebClientCallResult(HttpStatusCode status,
                                                                        Headers headers,
                                                                        T data,
                                                                        long elapsedMillis) {
    HttpClientResponse<T> response = new HttpClientResponse<T>(status, headers, data);
    return new WebClientCallResult<>(response, elapsedMillis);
  }

  /**
   * HTTP 응답 상태 코드를 반환합니다.
   *
   * @return {@link HttpStatusCode}
   */
  public HttpStatusCode getHttpStatus() {
    return response.getHttpStatus();
  }

  /**
   * 호출이 성공(HTTP 2xx)인지 여부를 반환합니다.
   *
   * @return 성공 시 {@code true}, 실패 시 {@code false}
   */
  public boolean isSuccess() {
    return getHttpStatus().is2xxSuccessful();
  }

  /**
   * 응답 헤더를 반환합니다.
   *
   * @return {@link Headers}
   */
  public Headers getHeaders() {
    return response.getHeaders();
  }

  /**
   * 응답 본문 데이터를 반환합니다.
   *
   * @return 응답 데이터
   */
  public T getData() {
    return response.getData();
  }

  /**
   * WebClient 호출에 소요된 시간을 밀리초 단위로 반환합니다.
   *
   * @return 호출 시간(ms)
   */
  public long getElapsedMillis() {
    return this.elapsedMillis;
  }

  /**
   * WebClient의 상태 코드, 응답 헤더, 본문 데이터를 캡슐화한 내부 클래스입니다.
   *
   * <p>외부에서는 직접 사용할 필요 없이, {@link WebClientCallResult}를 통해 간접적으로 접근합니다.
   *
   * @param <T> 응답 본문 타입
   */
  private static class HttpClientResponse<T> {
    /** HTTP 상태 코드. */
    private final HttpStatusCode httpStatus;
    /** 응답 헤더. */
    private final Headers headers;
    /** 응답 본문. */
    private final T data;

    private HttpClientResponse(HttpStatusCode status, Headers headers, T data) {
      this.httpStatus = status;
      this.headers = headers;
      this.data = data;
    }

    public HttpStatusCode getHttpStatus() {
      return this.httpStatus;
    }

    public Headers getHeaders() {
      return this.headers;
    }

    /**
     * 응답 본문을 반환합니다.
     *
     * @return 응답 본문 데이터
     */
    public T getData() {
      return this.data;
    }
  }

}
