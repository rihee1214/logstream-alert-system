package com.rihee.alerting.common.actuator;

import static com.rihee.alerting.common.log.enums.StructuredLogProperties.SERVICE;
import static com.rihee.alerting.common.log.enums.StructuredLogProperties.META;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.rihee.alerting.common.log.enums.LogType.ACT;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

// TODO 기본 MDC 설정을 어디서 해줄지 고민해야함. 다른 요소들은 interceptor에서 알아서 처리해 주지만 여기는 다름.
@Component
@ConditionalOnProperty(name = "monitoring.scheduler.enable", havingValue = "true", matchIfMissing = false)
public class CommonMonitoringScheduler {

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  //noinspection connectTimeout
  @Value("${monitoring.timeout.connect:PT3S}")
  private Duration connectTimeout = Duration.ofSeconds(3);

  //noinspection readTimeout
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
   * Actuator 정보를 수집하기 위한 초기화.
   *
   * @param env 환경변수 (포트 정보 가져오기 위함)
   */
  public CommonMonitoringScheduler(Environment env, @Value("${service.name:}") String serviceName) {
    this.serviceName = StringUtils.hasText(serviceName) ? serviceName : "unknown-service";
    String port = env.getProperty("server.port", env.getProperty("local.server.port", "8080"));

    // TIME OUT 세팅용
    HttpClient client = HttpClient.create()
                                .responseTimeout(this.readTimeout)
                                .option(CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());

    this.httpClient = WebClient.builder()
                                .baseUrl("http:localhost:" + port)
                                .clientConnector(new ReactorClientHttpConnector(client))
                                .build();
  }

  // TODO ACTUATOR가 오로지 로컬 호스트에서 오는 요청만 받도록 해야함. (혹은 filebeat가 다른 서버에 있다면 해당 서버도 포함)
  @Scheduled(fixedDelay = 10000)
  public void checkHealthLog() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    MDC.put(SERVICE.getName(), serviceName);
    String method = HttpMethod.GET.toString();
    String uri = "/actuator/health";
    httpClient.get()
              .uri(uri)
              .exchangeToMono(response -> {
                stopWatch.stop();
                HttpStatusCode status = response.statusCode();
                int statusCode = status.value();
                String statusMessage = (status instanceof HttpStatus)
                    ? ((HttpStatus) status).getReasonPhrase()
                    : HttpStatus.valueOf(statusCode).getReasonPhrase();
                Map<String, Object> rawMeta = Map.of(
                    "method", method,
                    "uri", uri,
                    "statusCode", statusCode,
                    "statusMessage", statusMessage,
                    "elapsedMs", stopWatch.getTotalTimeMillis()
                );
                String meta = null;
                try {
                  meta = jsonMapper.writeValueAsString(rawMeta);
                } catch (JsonProcessingException e) {
                  // TODO 어떻게 해결할지 고민해봐야 한다.
                }
                MDC.put(META.getName(), meta);
                return response.bodyToMono(String.class).flatMap(body -> {
                  try {
                    if (response.statusCode().is2xxSuccessful()) {
                      logger.info(ACT, body);
                    } else if (response.statusCode().isError()) {
                      logger.warn(ACT, body);
                    }
                  } finally {
                    MDC.clear();
                  }
                  return Mono.empty();
                });
              })
              .doOnError(ex -> {
                stopWatch.stop();
                Map<String, Object> rawMeta = Map.of(
                    "method", method,
                    "uri", uri,
                    "elapsedMs", stopWatch.getTotalTimeMillis()
                );
                String meta = null;
                try {
                  meta = jsonMapper.writeValueAsString(rawMeta);
                } catch (JsonProcessingException e) {
                  // TODO 어떻게 해결할지 고민해봐야 한다.
                }
                MDC.put(META.getName(), meta);
                logger.debug(ACT, "During Health Check", ex);
              })
              .block();
    MDC.clear();
  }

//  @Scheduled(fixedDelay = 10000)
//  public void checkMetricLog() {
//    String metric = httpClient.get().uri("/actuator/metric")
//                                    .retrieve().bodyToMono(String.class).block();
//    logger.info(ACT, metric);
//    MDC.clear();
//  }

}
