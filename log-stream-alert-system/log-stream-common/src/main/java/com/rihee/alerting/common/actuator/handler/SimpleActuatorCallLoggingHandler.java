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
import java.util.Arrays;
import java.util.Collections;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@code SimpleActuatorCallLoggingHandler}는 단일 actuator endpoint에 대해
 * GET 요청을 전송하고, 응답 결과를 구조화된 로그로 기록하는 기본 핸들러입니다.
 *
 * <p>이 핸들러는 {@link ActuatorCallLoggingHandler} 인터페이스의 구현체로서,
 * {@code /actuator/health}와 같은 단일 endpoint 호출에 최적화되어 있습니다.</p>
 *
 * <p>요청은 WebClient를 통해 비동기적으로 전송되며, 내부적으로 소요 시간 측정 및
 * MDC 기반 메타 정보 설정을 수행합니다. 성공 여부 및 상태 코드에 따라
 * 적절한 로그 레벨(info/warn)로 메시지를 출력합니다.</p>
 *
 * <p>{@code monitoring.scheduler.enable=true} 조건 하에서
 * {@link com.rihee.alerting.common.actuator.CommonMonitoringScheduler}에 의해 주기적으로 실행됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleActuatorCallLoggingHandler implements ActuatorCallLoggingHandler {

  private static final StructuredLogger logger
                      = StructuredLoggerFactory.getLogger(SimpleActuatorCallLoggingHandler.class);

  /**
   * actuator endpoint를 호출하고, 응답 결과를 구조화된 로그로 기록합니다.
   *
   * <p>설정 파일에서 대상 URI 목록을 조회한 후, 각 URI에 대해 비동기 요청을 수행하고,
   * 응답 메타데이터(MDC), 상태 코드, 메시지 등을 포함한 structured log를 출력합니다.
   *
   * <p>요청은 {@link WebClient}를 통해 비동기 전송되며,
   * 응답은 {@code .block()} 호출을 통해 동기적으로 대기하여 처리됩니다.
   *
   * @param client       actuator 호출에 사용할 WebClient 인스턴스
   * @param properties   설정 파일에서 로드된 프로퍼티 (URI 정보 등 포함)
   * @param serviceName  로그에 포함될 서비스 식별자
   */
  @Override
  public void execute(WebClient client, Properties properties, String serviceName) {
    // TODO URL 찾아서 seperator 로 분리 시키는 작업이 필요.
    // TODO 모든 작업이 빠르게 끝날 수 있도록, 모든 작업을 비동기 방식으로 처리하게 만들 필요가 있음.
    String[] uris = properties.getProperty("").split(",");

    Flux.merge(
            Arrays.stream(uris)
                .map(uri -> callActuatorEndpoint(client, uri, serviceName))
                .toList()
        )
        .then()
        .block();
  }

  /**
   * 지정된 actuator URI에 대해 WebClient를 사용해 GET 요청을 보내고,
   * 응답을 처리하여 구조화 로그를 남깁니다.
   *
   * <p>요청 소요 시간은 {@link StopWatch}로 측정되며, 로그 메타 정보는 MDC에 설정됩니다.
   * 요청은 비동기 처리되며, 응답은 {@code handleActuatorResponse}, 예외는 {@code handleActuatorError}에서 처리됩니다.
   *
   * @param client      actuator 호출용 WebClient
   * @param uri         호출할 actuator endpoint URI
   * @param serviceName 로그에 포함될 서비스 이름
   * @return 요청 완료를 나타내는 Mono 흐름
   */
  private Mono<Void> callActuatorEndpoint(WebClient client, String uri, String serviceName) {
    MDC.put(SERVICE.getName(), serviceName);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    return client.get()
        .uri(uri)
        .exchangeToMono(response -> handleActuatorResponse(response, stopWatch, uri))
        .doOnError(ex -> handleActuatorError(ex, stopWatch, uri))
        .doFinally(signalType -> MDC.clear())
        .then();
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
