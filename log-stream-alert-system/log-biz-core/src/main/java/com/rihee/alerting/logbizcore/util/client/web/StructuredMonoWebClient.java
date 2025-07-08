package com.rihee.alerting.logbizcore.util.client.web;

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
import com.rihee.alerting.logbizcore.util.client.web.response.WebClientCallResult;
import java.util.Map;
import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * {@code StructuredMonoWebClient}는 WebClient를 래핑하여,
 * HTTP 호출 시 로그, 추적 정보 전파, 응답 래핑을 일관되게 처리하는 헬퍼 유틸리티입니다.
 *
 * <p>다음 기능들을 제공합니다:
 * <ul>
 *   <li>요청 시 traceId, spanId, parentSpanId를 B3 Header로 자동 삽입</li>
 *   <li>응답 도달 시 elapsed 시간 측정 및 structured BIZ 로그 출력</li>
 *   <li>응답 본문은 {@link WebClientCallResult}로 래핑되어 반환</li>
 *   <li>실패 시 {@code Mono.error(Throwable)}를 통해 상위로 예외 전파</li>
 * </ul>
 *
 * <p>요청 데이터 타입과 응답 데이터 타입은 서로 다를 수 있으며,
 * 타입 유추를 위해 {@link ParameterizedTypeReference}를 기반으로 동작합니다.
 *
 * @author 리희
 * @since 1.0
 */
public class StructuredMonoWebClient {

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
   * HTTP 메서드와 URI, 요청 객체 및 응답 클래스를 기반으로 HTTP 요청을 수행합니다.
   *
   * <p>{@link ParameterizedTypeReference}를 직접 제공하지 않고 {@link Class}를 통해 응답 타입을 간단히 지정할 수 있습니다.
   *
   * <p>내부적으로 {@code ParameterizedTypeReference.forType(respType)}로 변환하여 실제 요청을 처리합니다.
   *
   * @param <T> 요청 본문의 타입
   * @param <R> 응답 본문의 타입
   * @param method HTTP 메서드 (예: GET, POST 등)
   * @param uri 호출할 URI
   * @param data 요청 본문 객체
   * @param respType 응답 타입 클래스 객체
   * @return 응답을 포함하는 {@code Mono<WebClientCallResult<R>>}
   */
  public <T, R> Mono<WebClientCallResult<R>> executeMonoCall(String method,
                                                      String uri,
                                                      T data,
                                                      Class<R> respType) {
    HttpMethod httpMethod = HttpMethod.valueOf(method);
    return executeMonoCall(httpMethod, uri, data, ParameterizedTypeReference.forType(respType));
  }

  /**
   * 문자열 기반 HTTP 메서드와 URI, 요청 객체 및 응답 클래스를 기반으로 HTTP 요청을 수행합니다.
   *
   * <p>문자열로 전달된 HTTP 메서드를 {@link HttpMethod#valueOf(String)}를 통해 Enum으로 변환하여 처리합니다.
   *
   * <p>응답 타입은 {@code Class<R>}를 통해 지정되며 내부적으로 {@link ParameterizedTypeReference}로 변환됩니다.
   *
   * @param <T> 요청 본문의 타입
   * @param <R> 응답 본문의 타입
   * @param method 문자열로 전달된 HTTP 메서드 (예: "GET", "POST")
   * @param uri 호출할 URI
   * @param data 요청 본문 객체
   * @param respType 응답 타입 클래스 객체
   * @return 응답을 포함하는 {@code Mono<WebClientCallResult<R>>}
   */
  public <T, R> Mono<WebClientCallResult<R>> executeMonoCall(String method, String uri, T data,
                                                          ParameterizedTypeReference<R> respType) {
    HttpMethod httpMethod = HttpMethod.valueOf(method);
    return executeMonoCall(httpMethod, uri, data, respType);
  }

  /**
   * 문자열 기반 HTTP 메서드와 URI, 요청 객체 및 {@link ParameterizedTypeReference}를 사용하여 HTTP 요청을 수행합니다.
   *
   * <p>{@code respType}을 통해 제네릭 응답 타입을 명시적으로 지정할 수 있습니다.
   * 예를 들어 List, Map, Wrapper 객체 등도 처리 가능합니다.
   *
   * @param <T> 요청 본문의 타입
   * @param <R> 응답 본문의 타입
   * @param method 문자열로 전달된 HTTP 메서드 (예: "GET", "POST")
   * @param uri 호출할 URI
   * @param data 요청 본문 객체
   * @param respType 응답 타입을 나타내는 {@link ParameterizedTypeReference}
   * @return 응답을 포함하는 {@code Mono<WebClientCallResult<R>>}
   */
  public <T, R> Mono<WebClientCallResult<R>> executeMonoCall(HttpMethod method, String uri, T data,
                                                                                Class<R> respType) {
    return executeMonoCall(method, uri, data, ParameterizedTypeReference.forType(respType));
  }

  /**
   * WebClient를 사용해 HTTP 요청을 수행하고, structured 로그 및 응답 래핑을 처리합니다.
   *
   * <p>요청과 응답의 타입이 다를 수 있으므로, {@code data}와 {@code respType}은
   * 서로 다른 제네릭 타입일 수 있습니다.
   *
   * <p>요청 시 B3 헤더를 포함하고, 응답 도달 시 elapsed 시간 및 상태 코드를 기반으로
   * 표준화된 로그를 출력합니다.
   *
   * <p>정상 응답은 {@link WebClientCallResult}로 래핑되어 반환되며,
   * 에러는 {@code Mono.error(...)}로 전파됩니다.
   *
   * @param <T> 요청 객체 타입
   * @param <R> 응답 객체 타입
   * @param method HTTP 메서드 (예: GET, POST 등)
   * @param uri 호출할 URI
   * @param data 요청 본문 객체
   * @param respType 응답 타입을 나타내는 {@link ParameterizedTypeReference}
   * @return 응답을 포함하는 {@code Mono<WebClientCallResult<R>>}
   */
  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  public <T, R> Mono<WebClientCallResult<R>> executeMonoCall(HttpMethod method, String uri, T data,
                                                          ParameterizedTypeReference<R> respType) {
    Map<String, String> snapshot = MDC.getCopyOfContextMap();
    validateContext(snapshot);

    //타이머 세팅
    long start = System.nanoTime();
    return wc.method(method)
        .uri(uri)
        // 기본 요청 헤더 세팅
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
        // MDC 정보를 세팅 후 로깅하여 비동기 환경에서 추적이 가능한 단초를 제공하고, response를 객체로 만들어 제공
        .exchangeToMono(resp -> {
          MDC.setContextMap(snapshot);
          long elapsed = (System.nanoTime() - start) / 1_000_000;

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
              Objects.toString(resp.headers()
                                  .header(B3Header.TRACE_ID.getHeaderName()).getFirst(),
                                          DefaultValues.UNKNOWN.getValue()));
          MDC.put(ELAPSED_MS.getKey(), String.valueOf(elapsed));

          logger.info(LogType.BIZ,
              "External call completed | uri={} | method={} | statusCode={} | elapsedMs={}ms | "
                  + "traceId={} | spanId={} | remoteTraceId={}",
              uri,
              resp.request().getMethod(),
              status.value(),
              elapsed,
              MDC.get(TRACE_ID.getName()),
              MDC.get(SPAN_ID.getName()),
              MDC.get(RESP_TRACE_ID.getKey())
          );

          MDC.clear();
          // 개발자가 실 환경에서 사용할 수 있을만한 구조로 response를 담아 Mono로 return
          return resp.bodyToMono(respType)
                  .map(body -> {
                    return WebClientCallResult.processedWebClientCallResult(status, resp.headers(),
                                                             body, elapsed);
                  });
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
