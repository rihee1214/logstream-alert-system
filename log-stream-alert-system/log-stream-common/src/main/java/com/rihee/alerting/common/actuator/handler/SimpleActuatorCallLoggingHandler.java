package com.rihee.alerting.common.actuator.handler;

import static com.rihee.alerting.common.log.constant.LogType.ACT;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.META;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SERVICE;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.ELAPSED_MS;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.METHOD;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.STATUS_CODE;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.STATUS_MESSAGE;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.util.SimpleJsonUtils;
import java.util.Map;
import java.util.Properties;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleActuatorCallLoggingHandler implements ActuatorCallLoggingHandler {

  private static final StructuredLogger logger
                      = StructuredLoggerFactory.getLogger(SimpleActuatorCallLoggingHandler.class);

  @Override
  public void execute(WebClient client, Properties properties, String serviceName) {
    MDC.put(SERVICE.getName(), serviceName);
    String uri = properties.getProperty("");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    client.get()
        .uri(uri)
        .exchangeToMono(response -> handleActuatorResponse(response, stopWatch, uri))
        .doOnError(ex -> handleActuatorError(ex, stopWatch, uri))
        .block();
    MDC.clear();
  }

  /**
   * actuator 요청의 성공 응답을 처리하여 구조화 로그로 기록합니다.
   *
   * <p>HTTP 상태 코드, 상태 메시지, 요청 소요 시간, URI 등 메타 정보를 MDC에 설정하며,
   * 응답 본문은 로그 레벨에 따라 기록됩니다.
   *
   * @param response 응답 객체
   * @param stopWatch 소요 시간 측정을 위한 StopWatch
   * @param uri 요청한 actuator URI
   * @return 빈 Mono 객체 (Reactive 흐름 완료)
   */
  private Mono<?> handleActuatorResponse(ClientResponse response, StopWatch stopWatch, String uri) {
    // 메타 정보 준비
    stopWatch.stop();
    HttpStatusCode status = response.statusCode();
    int statusCode = status.value();
    String statusMessage = (status instanceof HttpStatus)
        ? ((HttpStatus) status).getReasonPhrase()
        : HttpStatus.valueOf(statusCode).getReasonPhrase();

    // 로깅 전 메타 정보 세팅
    Map<String, Object> rawMeta = Map.of(
        METHOD.getKey(), response.request().getMethod().name(),
        URI.getKey(), uri,
        STATUS_CODE.getKey(), statusCode,
        STATUS_MESSAGE.getKey(), statusMessage,
        ELAPSED_MS.getKey(), stopWatch.getTotalTimeMillis()
    );
    putRawMetaToMdc(rawMeta);

    // 실질 로깅 작업
    return response.bodyToMono(String.class).flatMap(body -> {
      if (response.statusCode().is2xxSuccessful()) {
        logger.info(ACT, body);
      } else if (response.statusCode().isError()) {
        logger.warn(ACT, body);
      }
      return Mono.empty();
    });
  }

  /**
   * actuator 요청 중 예외가 발생했을 때 호출되는 메서드로,
   * 예외 내용을 구조화된 로그와 함께 기록합니다.
   *
   * <p>요청 URI, HTTP method, 소요 시간 등의 메타 정보를 함께 포함합니다.
   *
   * @param ex 발생한 예외
   * @param stopWatch 요청 소요 시간 측정용 StopWatch
   * @param uri 호출한 actuator URI
   */
  private void handleActuatorError(Throwable ex, StopWatch stopWatch, String uri) {
    // 로깅 전 메타 정보 세팅
    stopWatch.stop();
    Map<String, Object> rawMeta = Map.of(
        METHOD.getKey(), HttpMethod.GET.name(),
        URI.getKey(), uri,
        ELAPSED_MS.getKey(), stopWatch.getTotalTimeMillis()
    );
    putRawMetaToMdc(rawMeta);
    // 실질 로깅작업
    logger.warn(ACT, "During Actuator Call", ex);
  }

  /**
   * 주어진 메타 정보를 JSON 문자열로 변환하여 MDC에 설정합니다.
   *
   * <p>변환 실패 시 빈 JSON 객체 ({@code {}})를 대체값으로 사용하며,
   * 내부 변환 오류도 로그에 기록합니다.
   *
   * @param rawMeta 로그 메타 정보 Map
   */
  private void putRawMetaToMdc(Map<String, Object> rawMeta) {
    String meta = "{}";
    try {
      meta = SimpleJsonUtils.writeValueAsString(rawMeta);
    } catch (JsonProcessingException e) {
      logger.warn(ACT, "Meta 정보를 JSON 문자열로 변환하는 중 오류가 발생했습니다.", e);
    }
    MDC.put(META.getName(), meta);
  }
}
