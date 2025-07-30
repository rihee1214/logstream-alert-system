package com.rihee.alerting.logbizcore.actuator;

import static com.rihee.alerting.common.constant.log.CallCommonProperties.ELAPSED_MS;
import static com.rihee.alerting.common.constant.log.CallCommonProperties.TYPE;
import static com.rihee.alerting.common.constant.log.CallType.HTTP;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.METHOD;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.STATUS_CODE;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.STATUS_MESSAGE;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.URI;
import static com.rihee.alerting.common.constant.log.LogType.ACT;
import static com.rihee.alerting.common.constant.log.LogType.SYS;
import static com.rihee.alerting.common.constant.log.StructuredLogProperties.SERVICE;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

import com.rihee.alerting.common.constant.DefaultValues;
import com.rihee.alerting.logbizcore.log.StructuredLogger;
import com.rihee.alerting.logbizcore.log.StructuredLoggerFactory;
import java.time.Duration;
import java.time.format.DateTimeParseException;
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
 * {@code ActuatorHealthMonitoringScheduler}는 Spring Boot 애플리케이션의 {@code /actuator/health} endpoint를
 * 주기적으로 호출하여 구조화된 로그로 상태를 기록하는 전용 self-monitoring 스케줄러입니다.
 *
 * <p>이 스케줄러는 WebClient를 통해 애플리케이션 내부의 health endpoint를 호출하고,
 * 응답 결과 및 상태 코드를 기반으로 structured log를 출력합니다. 로그에는 HTTP 상태, 응답 본문,
 * 요청 URI, 요청 소요 시간 등의 메타데이터가 포함되며, MDC를 이용해 JSON 기반 로그로 기록됩니다.
 *
 * <p><strong>이 구성은 외부 모니터링 시스템(Prometheus + AlertManager)과는 별도로 동작하며,</strong>
 * 외부 모니터링이 실패하거나 지연되는 상황에서도 애플리케이션 내부에서 상태 변화를 기록할 수 있는
 * 보조적인 관찰 수단(self-observation)으로 설계되었습니다.
 *
 * <p>예를 들어, Prometheus가 scrape에 실패하거나 exporter가 일시적으로 중단된 경우에도,
 * 이 스케줄러는 서비스의 응답 지연, health 상태 변화, 장애 직전의 응답 추이 등을 기록함으로써
 * 사후 분석(post-mortem)과 원인 추적(root cause analysis)에 유용한 단서를 제공합니다.
 *
 * <p>서비스가 완전히 종료되면 해당 로그도 남기지 못하므로, 장애 탐지의 "주체"가 되기보다는,
 * "장애 전후 맥락을 남기는 기록자"로의 역할을 수행합니다.
 *
 * <p><strong>이 컴포넌트는 {@code monitoring.scheduler.enabled=true}일 경우에만 활성화되며,</strong>
 * 운영 환경에서는 기본적으로 활성화되지만, 개발 환경에서는 해당 설정을 비활성화함으로써
 * 불필요한 구조화 로그 생성을 피할 수 있도록 설계되어 있습니다.
 * 설정을 생략한 경우에도 기본값은 {@code true}로 간주되며, 명시적으로 꺼야 비활성화됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@Component
@ConditionalOnProperty(
    prefix = "monitoring.scheduler",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ActuatorHealthMonitoringScheduler {

  private final Duration connectTimeout;

  private final Duration readTimeout;

  private final String actuatorBaseUrl;
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
   * <p>이 스케줄러는 {@code /actuator/health} 엔드포인트를 주기적으로 호출하여
   * 서비스의 내부 상태를 점검하고, 그 결과를 structured log로 기록합니다.
   *
   * <p>{@code service.name}은 structured log에 포함될 서비스 식별자로,
   * 설정되지 않은 경우 애플리케이션 기동이 실패합니다.
   *
   * <p>{@code monitoring.timeout.connect}, {@code monitoring.timeout.read} 값은 ISO-8601 형식으로 주입되며,
   * {@code PT3S}가 기본값입니다. 잘못된 포맷일 경우 예외가 발생합니다.
   *
   * @param env Spring {@link Environment} 객체. {@code server.port} 확인에 사용됩니다.
   * @param serviceName structured log에 삽입될 서비스 이름
   * @param connectTimeout actuator call의 연결 타임아웃 (ISO-8601 형식, ex: PT3S)
   * @param readTimeout actuator call의 응답 타임아웃 (ISO-8601 형식)
   * @throws IllegalStateException 필수 설정 누락 또는 duration 포맷이 잘못된 경우
   */
  public ActuatorHealthMonitoringScheduler(Environment env,
                                          @Value("${service.name}") String serviceName,
      @Value("${monitoring.timeout.connect:PT3S}") String connectTimeout,
      @Value("${monitoring.timeout.read:PT3S}") String readTimeout) {
    if (!StringUtils.hasText(serviceName)) {
      throw new IllegalStateException(
          "Missing required configuration: 'service.name'. "
              + "Please set it using -Dservice.name or environment variable."
      );
    }

    this.serviceName = serviceName;
    String actuatorPort = env.getProperty("server.port");

    if (!StringUtils.hasText(actuatorPort)) {
      logger.warn(SYS, "[ActuatorSelfMonitor] Failed to resolve actuator port. "
          + "Please set 'server.port'. Using default: 8080");
      actuatorPort = "8080";
    }

    try {
      this.readTimeout = Duration.parse(readTimeout);
    } catch (DateTimeParseException e) {
      throw new IllegalStateException(
          "Invalid duration format for 'monitoring.timeout.read': " + readTimeout, e
      );
    }

    try {
      this.connectTimeout = Duration.parse(connectTimeout);
    } catch (DateTimeParseException e) {
      throw new IllegalStateException(
          "Invalid duration format for 'monitoring.timeout.connect': " + connectTimeout, e
      );
    }

    String actuatorBaseUrl = env.getProperty("management.endpoints.web.base-path");
    if (!StringUtils.hasText(actuatorBaseUrl)) {
      logger.warn(SYS, "[ActuatorSelfMonitor] Failed to resolve actuator Base Url. "
          + "Please set 'management.endpoints.web.base-path'. Using default: '/actuator'");
      actuatorBaseUrl = "/actuator";
    }
    this.actuatorBaseUrl = actuatorBaseUrl;

    // TIME OUT 세팅용
    HttpClient client = HttpClient.create()
                                .responseTimeout(this.readTimeout)
                                .option(CONNECT_TIMEOUT_MILLIS,
                                          (int) this.connectTimeout.toMillis());

    this.httpClient = WebClient.builder()
                                .baseUrl("http://localhost:" + actuatorPort)
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
    String uri = this.actuatorBaseUrl + "/health";
    MDC.put(SERVICE.getFieldName(), serviceName);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    httpClient.get()
        .uri(uri)
        .exchangeToMono(response -> handleActuatorResponse(response, stopWatch, uri))
        .doOnError(ex -> handleActuatorError(ex, stopWatch, uri))
        .doFinally(signalType -> MDC.clear())
        .then()
        .block();
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

    String statusMessage;
    if (status instanceof HttpStatus) {
      statusMessage = ((HttpStatus) status).getReasonPhrase();
    } else {
      HttpStatus tempStatus = HttpStatus.resolve(statusCode);
      statusMessage = tempStatus != null
                              ? tempStatus.getReasonPhrase()
                              : DefaultValues.UNKNOWN.getValue();
    }

    Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
    // 실질 로깅 작업
    return response.bodyToMono(String.class).flatMap(body -> {
      try {
        // 로깅 전 메타 정보 세팅
        MDC.put(TYPE.getFieldName(), HTTP.getType());
        MDC.put(METHOD.getFieldKey(), response.request().getMethod().name());
        MDC.put(URI.getFieldKey(), uri);
        MDC.put(STATUS_CODE.getFieldKey(), String.valueOf(statusCode));
        MDC.put(STATUS_MESSAGE.getFieldKey(), statusMessage);
        MDC.put(ELAPSED_MS.getFieldName(), String.valueOf(stopWatch.getTotalTimeMillis()));

        if (response.statusCode().is2xxSuccessful()) {
          logger.info(ACT, body);
        } else if (response.statusCode().isError()) {
          logger.warn(ACT, body);
        }
      } finally {
        if (mdcSnapshot != null) {
          MDC.setContextMap(mdcSnapshot);
        } else {
          MDC.clear();
        }
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
    Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
    try {
      MDC.put(TYPE.getFieldName(), HTTP.getType());
      MDC.put(METHOD.getFieldKey(), HttpMethod.GET.name());
      MDC.put(URI.getFieldKey(), uri);
      MDC.put(ELAPSED_MS.getFieldName(), String.valueOf(stopWatch.getTotalTimeMillis()));
      // 실질 로깅작업
      logger.warn(ACT, "During Actuator Call", ex);
    } finally {
      MDC.setContextMap(mdcSnapshot);
    }
  }

}
