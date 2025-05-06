package com.rihee.alerting.common.actuator;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import io.netty.channel.ChannelOption;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static com.rihee.alerting.common.log.enums.LogType.ACT;

// TODO 기본 MDC 설정을 어디서 해줄지 고민해야함. 다른 요소들은 interceptor에서 알아서 처리해 주지만 여기는 다름.
@Component
@ConditionalOnProperty(name = "monitoring.scheduler.enable", havingValue = "true", matchIfMissing = false)
public class CommonMonitoringScheduler {

    //noinspection connectTimeout
    @Value("${monitoring.timeout.connect:PT3S}")
    private Duration connectTimeout = Duration.ofSeconds(3);

    //noinspection readTimeout
    @Value("${monitoring.timeout.read:PT3S}")
    private Duration readTimeout = Duration.ofSeconds(3);

    private final StructuredLogger logger = StructuredLoggerFactory.getLogger(CommonMonitoringScheduler.class);
    private final WebClient httpClient;

    /**
     *
     * @param env 환경변수 (포트 정보 가져오기 위함)
     */
    public CommonMonitoringScheduler(Environment env) {
        String port = env.getProperty("server.port", env.getProperty("local.server.port", "8080"));

        // TIME OUT 세팅용
        HttpClient client = HttpClient.create()
                                        .responseTimeout(this.readTimeout)
                                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());

        this.httpClient = WebClient.builder()
                                    .baseUrl("http:localhost:" + port)
                                    .clientConnector(new ReactorClientHttpConnector(client))
                                    .build();
    }

    // TODO ACTUATOR가 오로지 로컬 호스트에서 오는 요청만 받도록 해야함. (혹은 filebeat가 다른 서버에 있다면 해당 서버도 포함)
    @Scheduled(fixedDelay = 10000)
    public void checkHealthLog(){
        httpClient.get()
                    .uri("/actuator/health")
                    .exchangeToMono(response -> {
                        logResponseMetadata(response);
                        return Mono.empty();
                    })
                    .doOnError(ex -> {
                        // TODO MDC에 무엇을 넣을지 고민
//                        MDC.put(STACK_TRACE.getName(), ex.getMessage());
                        logger.debug(ACT, "During Health Check", ex);
                    })
                    .block();
        MDC.clear();
    }

    @Scheduled(fixedDelay = 10000)
    public void checkMetricLog(){
        String metric = httpClient.get().uri("/actuator/metric").retrieve().bodyToMono(String.class).block();
        logger.info(ACT, metric);
        MDC.clear();
    }

    /**
     * 들어온 응답을 가지고 로깅
     * @param response 메트릭, 혹은 health 정보 요청에 대한 응답
     */
    private void logResponseMetadata(@NonNull ClientResponse response){
        response.bodyToMono(String.class).doOnNext(body -> {
            if(response.statusCode().is2xxSuccessful()){
                logger.info(ACT, body);
            } else if (response.statusCode().isError()) {
                logger.info(ACT, "");
            }

        });

    }
}
