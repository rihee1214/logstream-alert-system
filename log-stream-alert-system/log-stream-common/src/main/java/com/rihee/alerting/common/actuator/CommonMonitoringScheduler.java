package com.rihee.alerting.common.actuator;

import static com.rihee.alerting.common.constant.DefaultValues.LOGGING_DEFAULT_VALUE;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihee.alerting.common.actuator.handler.ActuatorCallLoggingHandler;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
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

  private final List<ActuatorCallLoggingHandler> handlers;
  /**
   * serviceName 로그에 포함될 서비스 이름(예: user-service).
   */
  private final String serviceName;

  private final StructuredLogger logger
            = StructuredLoggerFactory.getLogger(CommonMonitoringScheduler.class);
  private final WebClient httpClient;


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

  @Scheduled(fixedDelayString = "${monitoring.scheduler.interval.ms:10000}")
  public void scheduleActuatorLogs() {
    // TODO Properties는 가져올 수 있도록 코딩해야함
    // TODO 해당 작업들은 모두 서로에 영향이 없기 때문에 동시에 작동하도록 하는게 좋아보임
    handlers.forEach(handler -> handler.execute(this.httpClient, new Properties(), serviceName));
  }

}
