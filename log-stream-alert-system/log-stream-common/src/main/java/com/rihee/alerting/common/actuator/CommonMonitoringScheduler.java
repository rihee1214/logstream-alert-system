package com.rihee.alerting.common.actuator;

import static com.rihee.alerting.common.constant.DefaultValues.LOGGING_DEFAULT_VALUE;
import static com.rihee.alerting.common.log.constant.LogType.ACT;
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
import java.time.Duration;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * {@code /actuator/health}, {@code /actuator/metrics} 정보를 주기적으로 조회하여
 * 구조화된 로그로 기록하는 공통 모니터링 스케줄러입니다.
 *
 * <p>이 클래스는 Prometheus 또는 Filebeat와 같은 로그 수집 도구가
 * actuator 상태를 수집할 수 있도록, 내부적으로 WebClient를 통해
 * actuator endpoint를 직접 호출하고, 결과를 로그로 남기는 역할을 합니다.</p>
 *
 * <p>로그에는 호출 URI, HTTP 상태 코드, 응답 메시지, 소요 시간 등의 메타 정보를 함께 포함하여,
 * 문제 발생 시 서비스 상태 진단 및 추적이 가능하도록 설계되어 있습니다.</p>
 *
 * <p>이 스케줄러는 {@code monitoring.scheduler.enable=true}가 설정되어있는 경우에만 동작하며,
 * 컨테이너 내에서 로컬 actuator 호출만을 허용하는 환경을 전제로 합니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
@Component
@ConditionalOnProperty(name = "monitoring.scheduler.enable", havingValue = "true",
                                                                matchIfMissing = false)
public class CommonMonitoringScheduler {

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
            = StructuredLoggerFactory.getLogger(CommonMonitoringScheduler.class);
  private final WebClient httpClient;

  /**
   * 생성자: WebClient 구성 및 서비스 이름 설정을 초기화합니다.
   *
   * <p>주어진 {@code Environment}에서 포트 정보를 가져와
   * actuator endpoint를 호출할 수 있는 WebClient를 구성하며,
   * {@code service.name} 값이 없는 경우 {@code "LOGGING_DEFAULT_VALUE"}로 대체합니다.
   *
   * @param env Spring {@link Environment} 객체로부터 포트 정보와 설정 값을 주입받습니다.
   * @param serviceName 서비스 이름 (로그 필드에 사용됨)
   */
  public CommonMonitoringScheduler(Environment env, @Value("${service.name:}") String serviceName) {
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
   * actuator 상태 로그 수집을 주기적으로 실행하는 스케줄러 메서드입니다.
   *
   * <p>{@code /actuator/health}와 {@code /actuator/metrics} 경로를 호출하며,
   * 각각에 대한 응답 결과를 구조화된 로그로 기록합니다.
   *
   * <p>호출 주기는 프로퍼티 {@code monitoring.scheduler.interval.ms}의 값에 따라 결정되며,
   * 해당 값이 설정되지 않은 경우 기본값으로 10초({@code 10000ms})가 사용됩니다.
   *
   * <p>응답 시간 및 상태 코드를 포함한 메타 정보가 함께 로그에 기록됩니다.
   */
  @Scheduled(fixedDelayString = "${monitoring.scheduler.interval.ms:10000}")
  public void scheduleActuatorLogs() {
    logActuatorEndpoint("/actuator/health");
    logActuatorEndpoint("/actuator/metrics");
  }

  /**
   * 지정된 actuator endpoint URI를 WebClient를 통해 호출하고,
   * 응답 결과 또는 오류에 따라 구조화 로그를 기록합니다.
   *
   * @param uri actuator 경로 (예: {@code /actuator/health})
   */
  private void logActuatorEndpoint(String uri) {
    // Actuator 초기 세팅
    MDC.put(SERVICE.getName(), serviceName);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    httpClient.get()
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
      meta = jsonMapper.writeValueAsString(rawMeta);
    } catch (JsonProcessingException e) {
      logger.warn(ACT, "Meta 정보를 JSON 문자열로 변환하는 중 오류가 발생했습니다.", e);
    }
    MDC.put(META.getName(), meta);
  }

}
