package com.rihee.alerting.common.actuator;

import static com.rihee.alerting.common.constant.DefaultValues.LOGGING_DEFAULT_VALUE;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihee.alerting.common.actuator.handler.ActuatorCallLoggingHandler;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * {@code CommonMonitoringScheduler}는 actuator endpoint 정보를 주기적으로 조회하여
 * 구조화된 로그로 기록하는 공통 모니터링 스케줄러입니다.
 *
 * <p>이 클래스는 Prometheus, Filebeat 등의 로그 수집 도구가 actuator 상태를 간접 수집할 수 있도록,
 * WebClient를 통해 애플리케이션 내부의 {@code /actuator/health}, {@code /actuator/metrics} 등의 endpoint를
 * 호출하고, 결과를 structured log 형식으로 남깁니다.
 *
 * <p>호출된 actuator 응답에는 URI, HTTP 상태, 메시지, 응답 시간 등의 메타 정보가 포함되며,
 * 로그는 추후 장애 분석이나 운영 진단에 활용될 수 있습니다.
 *
 * <p>이 스케줄러는 {@code monitoring.scheduler.enable=true}로 설정된 경우에만 활성화되며,
 * 컨테이너 내부에서 로컬 actuator endpoint 호출을 전제로 합니다.
 *
 * <p>또한, 런타임 중 설정 파일 및 system property를 기반으로 스케줄러의 동작 여부를
 * 유동적으로 제어할 수 있도록 설계되어 있습니다.
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

  private final List<ActuatorCallLoggingHandler> handlers;
  /**
   * serviceName 로그에 포함될 서비스 이름(예: user-service).
   */
  private final String serviceName;

  private final StructuredLogger logger
            = StructuredLoggerFactory.getLogger(CommonMonitoringScheduler.class);
  private final WebClient httpClient;

  /**
   * {@code CommonMonitoringScheduler}의 인스턴스를 생성합니다.
   *
   * <p>이 생성자는 스케줄러가 사용할 actuator 핸들러 목록과 서비스 이름, 환경 정보를 주입받아
   * 내부에서 공용 WebClient를 구성하고, 로그 식별을 위한 serviceName 값을 설정합니다.
   * WebClient는 로컬 서버 포트를 기준으로 actuator endpoint에 접근하기 위해 동적으로 base URL을 구성합니다.</p>
   *
   * @param handlers      actuator 호출을 수행할 핸들러 리스트
   * @param env           Spring Environment 객체 (서버 포트 조회에 사용)
   * @param serviceName   로그에 포함될 서비스 이름. 값이 비어있을 경우 기본값이 사용됨
   */
  public CommonMonitoringScheduler(List<ActuatorCallLoggingHandler> handlers,
                                   Environment env,
                                   @Value("${service.name:}") String serviceName) {
    this.handlers = handlers;
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
   * actuator 관련 핸들러들을 주기적으로 실행하여 로그를 수집합니다.
   *
   * <p>이 메서드는 {@code @Scheduled} 어노테이션에 의해 일정 주기로 자동 호출되며,
   * {@code System.getProperty("monitoring.scheduler.enable")} 값에 따라 실행 여부를 동적으로 제어합니다.
   * 설정 경로는 {@code System.getProperty("metric.config.path")}를 통해 주입되며,
   * 해당 설정 파일은 각 handler의 실행 조건 및 메트릭 필터링 기준으로 활용됩니다.
   *
   * <p>실행 시 모든 {@link ActuatorCallLoggingHandler}를 병렬로 호출하여 비동기 방식으로 처리되며,
   * Netty 기반의 WebClient를 통해 actuator endpoint로 요청을 전송합니다.
   *
   * <p>모든 작업 완료 후에는 {@code .block()}을 통해 전체 흐름을 기다립니다.
   * 설정 파일이 존재하지 않거나 손상된 경우에는 {@link RuntimeException}을 발생시켜 스케줄링을 중단합니다.
   */
  @Scheduled(fixedDelayString = "${monitoring.scheduler.interval.ms:10000}")
  public void scheduleActuatorLogs() {
    // 시스템 과부하시, 동적으로 스케쥴러를 종료시킬 수 있도록 하는 스위치
    if (!Boolean.parseBoolean(System.getProperty("monitoring.scheduler.enable", "false"))) {
      logger.info(LogType.SYS, "스케줄러가 비활성화되어 있어 실행을 건너뜁니다.");
      return;
    }

    // Properties Init
    String propertiesPath = System.getProperty("metric.config.path");
    Properties configs = new Properties();
    try (InputStream in = Files.newInputStream(Paths.get(propertiesPath))) {
      configs.load(in);
    } catch (IOException e) {
      logger.error(LogType.SYS, "Actuator Call Scheduler 호출 시 설정 문제가 발생하였습니다.", e);
      throw new RuntimeException(e);
    }

    // 모든 handler 처리를 비동기적으로 처리하여 처리 속도를 증가시키고, 자원을 효율적으로 사용.
    Flux.merge(
            handlers.stream()
                .map(handler -> Mono.fromRunnable(() ->
                    handler.execute(httpClient, configs, serviceName)
                ))
                .toList()
        ).doOnError(e -> logger.error(LogType.SYS, "Actuator 핸들러 실행 중 오류 발생", e))
        .then().block(); // 전체 완료까지 대기
  }
}
