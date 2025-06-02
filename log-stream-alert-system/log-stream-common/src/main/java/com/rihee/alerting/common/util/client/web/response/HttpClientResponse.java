package com.rihee.alerting.common.util.client.web.response;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse.Headers;

public class HttpClientResponse<T> {

  private final ResponseStatus respStatus;
  private final HttpStatus httpStatus;
  private final Headers headers;
  private final T body;
  private final Throwable exception;
  private final Instant receivedAt;

  private HttpClientResponse(ResponseStatus respStatus, HttpStatus status,
                                                  Headers headers, T body, Throwable exception) {
    this.respStatus = respStatus;
    this.httpStatus = status;
    this.headers = headers;
    this.body = body;
    this.exception = exception;
    this.receivedAt = Instant.now();
  }

  public static <T> HttpClientResponse<T> success(HttpStatus status, Headers headers, T body) {
    if(status.is2xxSuccessful()) {
      return new HttpClientResponse<>(ResponseStatus.SUCCESS, status, headers, body, null);
    }
    return new HttpClientResponse<>(ResponseStatus.FAILURE, status, headers, body, null);
  }

  public static <T> HttpClientResponse<T> failure(Throwable ex) {
    return new HttpClientResponse<>(ResponseStatus.EXCEPTION, null, null, null, ex);
  }

  public boolean isSuccess() {
    return respStatus.equals(ResponseStatus.SUCCESS);
  }

  public boolean isFailure() {
    return respStatus.equals(ResponseStatus.FAILURE);
  }

  public boolean isErrored() {
    return respStatus.equals(ResponseStatus.EXCEPTION);
  }

  public enum ResponseStatus {
    SUCCESS,
    FAILURE,
    EXCEPTION
  }
}

