package com.rihee.alerting.common.actuator;

import static com.rihee.alerting.common.constant.DefaultValues.LOGGING_DEFAULT_VALUE;
import static com.rihee.alerting.common.log.constant.LogType.ACT;
import static com.rihee.alerting.common.log.constant.LogType.SYS;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.META;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SERVICE;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.ELAPSED_MS;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.METHOD;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.STATUS_CODE;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.STATUS_MESSAGE;
import static com.rihee.alerting.common.log.constant.biz.MetaProperties.URI;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.util.SimpleJsonUtils;
import java.time.Duration;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * {@code ActuatorHealthMonitoringScheduler}는 Spring Boot 애플리케이션의 {@code /actuator/health} endpoint를
 * 주기적으로 호출하여 구조화된 로그로 상태를 기록하는 전용 스케줄러입니다.
 *
 * <p>이 스케줄러는 WebClient를 통해 애플리케이션 내의 health endpoint를 호출하고,
 * 응답 결과 및 상태 코드를 기반으로 structured log를 출력합니다.
 *
 * <p>응답에는 HTTP 상태, 응답 본문, 요청 URI, 요청 소요 시간 등의 메타 데이터가 함께 포함되며,
 * MDC를 활용하여 JSON 기반 로그로 출력됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@Component
public class ActuatorHealthMonitoringScheduler {

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  @Value("${monitoring.timeout.connect:PT3S}")
  private Duration connectTimeout = Duration.ofSeconds(3);

  @Value("${monitoring.timeout.read:PT3S}")
  private Duration readTimeout = Duration.ofSeconds(3);

  /**
   * serviceName 로그에 포함될 서비스 이름(예: user-service).
   */
  private final String serviceName;

  private final StructuredLogger logger
            = StructuredLoggerFactory.getLogger(ActuatorHealthMonitoringScheduler.class);
  private final WebClient httpClient;

  /**
   * {@code ActuatorHealthMonitoringScheduler}의 인스턴스를 초기화합니다.
   *
   * <p>WebClient는 내부 서버의 포트를 기반으로 로컬 호출을 수행하도록 base URL을 설정하며,
   * {@code service.name}은 structured log에 포함될 서비스 이름으로 사용됩니다.
   *
   * @param env Spring Environment 객체 (server 포트 조회에 사용)
   * @param serviceName 로그에 포함될 서비스 식별자. 비어있으면 기본값 사용
   */
  public ActuatorHealthMonitoringScheduler(Environment env,
                                          @Value("${service.name:}") String serviceName) {
    this.serviceName = StringUtils.hasText(serviceName) ? serviceName
                                                        : LOGGING_DEFAULT_VALUE.getValue();
    String port = env.getProperty("server.port", env.getProperty("local.server.port", "8080"));

    // TIME OUT 세팅용
    HttpClient client = HttpClient.create()
                                .responseTimeout(this.readTimeout)
                                .option(CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());

    this.httpClient = WebClient.builder()
                                .baseUrl("http://localhost:" + port)
                                .clientConnector(new ReactorClientHttpConnector(client))
                                .build();
  }

  /**
   * {@code /actuator/health} endpoint를 주기적으로 호출하여 시스템 상태를 구조화 로그로 기록합니다.
   *
   * <p>이 메서드는 {@link org.springframework.scheduling.annotation.Scheduled @Scheduled}에 의해
   * 일정 주기로 자동 실행되며, 내부 WebClient를 사용해 health 상태를 조회합니다.
   * 호출 결과는 응답 코드, 메시지, 소요 시간 등의 메타 정보를 포함하여 structured log로 출력됩니다.
   *
   * <p>정상 응답은 info 레벨로, 오류 응답(4xx/5xx)이나 호출 실패는 warn 레벨로 기록됩니다.
   * 로그는 MDC를 통해 JSON 메타 데이터를 포함한 형태로 출력되며, 로그 수집 시스템 (e.g. Filebeat, Logstash)을 통해
   * 모니터링 및 시각화 시스템으로 전달될 수 있습니다.
   *
   * <p>이 구성은 Prometheus가 직접 health endpoint를 수집하지 못하는 환경에서,
   * 로그 기반으로 health 상태를 감시하는 용도로 사용됩니다.
   */
  @Scheduled(fixedDelayString = "${monitoring.scheduler.interval.ms:10000}")
  public void scheduleActuatorLogs() {
    String uri = "/health";
    MDC.put(SERVICE.getName(), serviceName);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    httpClient.get()
        .uri(uri)
        .exchangeToMono(response -> handleActuatorResponse(response, stopWatch, uri))
        .doOnError(ex -> handleActuatorError(ex, stopWatch, uri))
        .doFinally(signalType -> MDC.clear())
        .then();
  }

  /**
   * actuator health 응답을 처리하고, structured log로 기록합니다.
   *
   * <p>응답 본문, 상태 코드, 메시지, URI, 요청 소요 시간 등의 정보를 MDC에 기록하여 로그에 포함시킵니다.
   * 응답이 성공(2xx)일 경우 info 레벨, 오류(4xx/5xx)일 경우 warn 레벨로 기록됩니다.
   *
   * @param response actuator 응답 객체
   * @param stopWatch 요청 소요 시간 측정기
   * @param uri 호출한 actuator URI
   * @return 로그 기록 완료 후 Mono 흐름
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
   * actuator 요청 중 예외가 발생한 경우 호출되며, 예외 내용과 요청 정보를 structured log로 기록합니다.
   *
   * <p>MDC에는 요청 메서드, URI, 요청 소요 시간 등의 메타 정보가 포함되며,
   * 로그는 warn 레벨로 출력됩니다.
   *
   * @param ex 발생한 예외
   * @param stopWatch 요청 소요 시간 측정기
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
   * 지정된 메타 정보를 JSON 문자열로 직렬화하여 MDC에 설정합니다.
   *
   * <p>직렬화 중 오류가 발생한 경우, 빈 JSON 객체({@code {}})를 기본값으로 사용하며,
   * 변환 오류는 warn 레벨의 시스템 로그로 기록됩니다.
   *
   * @param rawMeta 로그에 포함할 메타 정보 Map
   */
  private void putRawMetaToMdc(Map<String, Object> rawMeta) {
    String meta = "{}";
    try {
      meta = SimpleJsonUtils.writeValueAsString(rawMeta);
    } catch (JsonProcessingException e) {
      logger.warn(SYS, "Meta 정보를 JSON 문자열로 변환하는 중 오류가 발생했습니다.", e);
    }
    MDC.put(META.getName(), meta);
  }
}
