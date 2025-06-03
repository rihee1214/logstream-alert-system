package com.rihee.alerting.common.util.client.web;

import static com.rihee.alerting.common.log.constant.CallCommonProperties.ELAPSED_MS;
import static com.rihee.alerting.common.log.constant.CallCommonProperties.TYPE;
import static com.rihee.alerting.common.log.constant.CallType.HTTP;
import static com.rihee.alerting.common.log.constant.HttpCallProperties.METHOD;
import static com.rihee.alerting.common.log.constant.HttpCallProperties.RESP_TRACE_ID;
import static com.rihee.alerting.common.log.constant.HttpCallProperties.STATUS_CODE;
import static com.rihee.alerting.common.log.constant.HttpCallProperties.STATUS_MESSAGE;
import static com.rihee.alerting.common.log.constant.HttpCallProperties.URI;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.TRACE_ID;

import com.rihee.alerting.common.constant.B3Header;
import com.rihee.alerting.common.constant.DefaultValues;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import com.rihee.alerting.common.util.client.web.response.WebClientCallResult;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * {@code StructuredMonoWebClient}는 WebClient 기반의 HTTP 요청을 수행하며,
 * 구조화된 로깅 및 추적 정보를 자동으로 설정하는 헬퍼 유틸 클래스입니다.
 *
 * <p>Micrometer Tracing 혹은 MDC 기반의 traceId, spanId, parentSpanId 등의 추적 정보를
 * HTTP 헤더(B3 Header)로 전달하며, 호출 시간과 응답 상태를 기반으로 StructuredLogger를 통해
 * 일관된 BIZ 로그를 출력합니다.
 *
 * <p>응답 성공 시 {@link WebClientCallResult}로 응답 본문 및 상태 정보를 래핑하며,
 * 실패 시 {@code Mono.error}로 예외를 전달하여 상위 레이어가 직접 처리하도록 위임합니다.
 *
 * @param <T> 요청 본문의 타입
 * @author 리희
 * @since 1.0
 */
public class StructuredMonoWebClient<T> {

  protected static final StructuredLogger logger
                    = StructuredLoggerFactory.getLogger(StructuredMonoWebClient.class);

  protected final WebClient wc;

  /**
   * 기본 생성자입니다.
   *
   * <p>내부적으로 {@code WebClient.builder().build()}를 사용하여 WebClient 인스턴스를 초기화합니다.
   */
  public StructuredMonoWebClient() {
    this(WebClient.builder());
  }

  /**
   * 커스텀 {@code WebClient.Builder}를 기반으로 인스턴스를 생성합니다.
   *
   * @param builder WebClient Builder 인스턴스
   */
  public StructuredMonoWebClient(WebClient.Builder builder) {
    wc = builder.build();
  }

  /**
   * 문자열로 주어진 HTTP 메서드와 URI를 기반으로 WebClient 호출을 수행합니다.
   *
   * @param method 요청 HTTP 메서드 (예: "POST", "GET")
   * @param uri 호출 대상 URI
   * @param data 요청 본문 (일반적으로 JSON 직렬화 대상 객체)
   * @return 응답 성공 시 {@code WebClientCallResult<String>}을 포함하는 {@code Mono}
   */
  public Mono<WebClientCallResult<String>> executeMonoCall(String method, String uri, T data) {
    HttpMethod httpMethod = HttpMethod.valueOf(method);
    return executeMonoCall(httpMethod, uri, data);
  }

  /**
   * 지정된 {@link HttpMethod}, URI, 요청 데이터를 기반으로 WebClient 비동기 호출을 수행합니다.
   *
   * <p>이 메서드는 다음과 같은 기능을 포함합니다:
   * <ul>
   *   <li>B3 Header 기반의 traceId, spanId, parentSpanId 전파</li>
   *   <li>MDC 컨텍스트를 통한 structured logging 정보 기록</li>
   *   <li>호출 성공 시 {@link WebClientCallResult} 반환</li>
   *   <li>호출 실패 시 {@code Mono.error(Throwable)}로 예외 전파</li>
   * </ul>
   *
   * <p>실패 케이스에 대해서는 별도의 로깅 처리 없이, 상위 호출자가 에러 흐름을 제어하도록 유도합니다.
   *
   * @param method HTTP 요청 방식 (예: GET, POST)
   * @param uri 호출 대상 URI
   * @param data 요청 본문 객체
   * @return 응답 성공 시 {@code WebClientCallResult<String>}을 포함하는 {@code Mono}
   * @throws IllegalStateException MDC에 traceId 또는 spanId가 누락된 경우
   */
  public Mono<WebClientCallResult<String>> executeMonoCall(HttpMethod method, String uri, T data) {
    StopWatch stopWatch = new StopWatch();
    Map<String, String> snapshot = MDC.getCopyOfContextMap();
    validateContext(snapshot);

    stopWatch.start();
    return wc.method(method)
        .uri(uri)
        .headers(httpHeaders -> {
          String traceId = snapshot.get(TRACE_ID.getName());
          String spanId = snapshot.get(SPAN_ID.getName());
          String parentSpanId = snapshot.get(PARENT_SPAN_ID.getName());

          httpHeaders.add(B3Header.TRACE_ID.getHeaderName(), traceId);
          httpHeaders.add(B3Header.SPAN_ID.getHeaderName(), spanId);
          if (parentSpanId != null) {
            httpHeaders.add(B3Header.PARENT_SPAN_ID.getHeaderName(), parentSpanId);
          }
        })
        .bodyValue(data)
        .exchangeToMono(resp -> {
          MDC.setContextMap(snapshot);
          stopWatch.stop();

          HttpStatusCode status = resp.statusCode();
          int statusCode = status.value();
          String statusMessage;
          if (status instanceof HttpStatus) {
            statusMessage = ((HttpStatus) status).getReasonPhrase();
          } else {
            HttpStatus tempStatus = HttpStatus.resolve(statusCode);
            statusMessage = tempStatus != null
                ? tempStatus.getReasonPhrase()
                : DefaultValues.UNKNOWN.getValue();
          }

          MDC.put(TYPE.getKey(), HTTP.getType());
          MDC.put(METHOD.getKey(), resp.request().getMethod().name());
          MDC.put(URI.getKey(), uri);
          MDC.put(STATUS_CODE.getKey(), String.valueOf(status.value()));
          MDC.put(STATUS_MESSAGE.getKey(), statusMessage);
          MDC.put(RESP_TRACE_ID.getKey(),
              resp.headers().header(B3Header.TRACE_ID.getHeaderName()).getFirst()
                  .describeConstable()
                  .orElse(DefaultValues.UNKNOWN.getValue()));
          MDC.put(ELAPSED_MS.getKey(), String.valueOf(stopWatch.getTotalTimeMillis()));

          logger.info(LogType.BIZ,
              "External call completed | uri={} | method={} | statusCode={} | elapsedMs={}ms | "
                  + "traceId={} | spanId={} | remoteTraceId={}",
              uri,
              resp.request().getMethod(),
              status.value(),
              stopWatch.getTotalTimeMillis(),
              MDC.get(TRACE_ID.getName()),
              MDC.get(SPAN_ID.getName()),
              MDC.get(RESP_TRACE_ID.getKey())
          );

          return resp.bodyToMono(String.class)
                  .map(body -> {
                    return WebClientCallResult.processedWebClientCallResult(status, resp.headers(),
                                                             body, stopWatch.getTotalTimeMillis());
                  });
        })
        .onErrorResume(throwable -> {
          stopWatch.stop();
          return Mono.error(throwable);
        });
  }

  /**
   * 현재 MDC 컨텍스트에서 traceId 및 spanId가 존재하는지 검증합니다.
   *
   * <p>해당 값이 누락된 경우 예외를 발생시켜 WebClient 호출을 차단합니다.
   *
   * @param snapshot MDC 컨텍스트 스냅샷
   * @throws IllegalStateException traceId 또는 spanId가 없을 경우
   */
  private static void validateContext(Map<String, String> snapshot) {
    if (snapshot == null || snapshot.isEmpty()) {
      throw new IllegalStateException(
          "MDC context is missing; required tracing keys not found."
      );
    }
    if (!snapshot.containsKey(TRACE_ID.getName()) || !snapshot.containsKey(SPAN_ID.getName())) {
      throw new IllegalStateException(
          "Missing required MDC keys: traceId and spanId must be present."
      );
    }
  }
}
