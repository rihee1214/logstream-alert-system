package com.rihee.alerting.mockservice.infra;

import com.rihee.alerting.logbizcore.log.StructuredLogger;
import com.rihee.alerting.logbizcore.log.StructuredLoggerFactory;
import com.rihee.alerting.common.constant.message.LogType;
import com.rihee.alerting.logbizcore.util.client.web.StructuredMonoWebClient;
import com.rihee.alerting.logbizcore.util.client.web.response.WebClientCallResult;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * {@code MockExternalCallImpl}은 {@link MockExternalCall} 인터페이스의 구현체로서,
 * mock 환경에서 외부 서비스 요청을 시뮬레이션하는 역할을 담당합니다.
 *
 * <p>이 클래스는 {@code WebClient}를 기반으로 외부 mock 서비스에 HTTP 요청을 전송하며,
 * 호출 결과를 구조화 로그로 기록하고, 예외 발생 시 fallback 응답을 반환합니다.
 * </p>
 *
 * <p>baseUrl은 Spring {@link Environment} 객체를 통해 {@code mockup.external.base-url}로부터 동적으로 주입되며,
 * 운영 환경(Kubernetes)에서는 서비스 이름, 개발 환경에서는 localhost로 설정될 수 있습니다.
 * </p>
 *
 * <p><strong>⚠️ 실패 처리:</strong></p>
 * <ul>
 *   <li>WebClient 호출 실패 시 WARN 로그 기록 후 fallback 문자열 반환</li>
 *   <li>예외 발생 시 ERROR 로그 기록 후 예외 메시지 반환</li>
 * </ul>
 *
 * @author 리희
 * @since 1.0
 */
@Component
public class MockExternalCallImpl implements MockExternalCall {

  /**
   * 테스트용 인증 헤더 이름 상수입니다.
   */
  private static final String MOCK_AUTH_TOKEN_HEADER = "X-Auth-Token";
  /**
   * 구조화 로그 출력을 위한 로거입니다.
   */
  private static final StructuredLogger logger
      = StructuredLoggerFactory.getLogger(MockExternalCallImpl.class);
  private StructuredMonoWebClient client;

  /**
   * {@code MockExternalCallImpl} 생성자입니다.
   *
   * <p>Spring {@link Environment}에서 {@code mockup.external.base-url}을 가져와 baseUrl을 설정하며,
   * 포트 정보가 없을 경우 기본값 {@code 8080}을 사용합니다.
   * </p>
   *
   * @param env Spring Environment 객체로부터 설정값을 주입 받음
   * @throws IllegalStateException {@code mockup.external.base-url}이 비어 있거나 존재하지 않으면 예외 발생
   */
  public MockExternalCallImpl(Environment env) {
    String baseUrl = env.getProperty("mockup.external.base-url");
    if (!StringUtils.hasText(baseUrl)) {
      throw new IllegalStateException(
          "Required property 'mockup.external.base-url' is not set or empty. "
          + "Please provide this value via application.yml, environment variable, or -D argument."
      );
    }

    String mockAuthToken = env.getProperty("mockup.token");
    if (!StringUtils.hasText(mockAuthToken)) {
      throw new IllegalStateException(
          "Missing required configuration: 'mockup.token'. "
              + "Please set it using -Dmockup.token or environment variable."
      );
    }

    WebClient.Builder clientBuilder = WebClient.builder().baseUrl(baseUrl)
        .filter(((request, next) -> {
          ClientRequest newReq
              = ClientRequest.from(request)
              .headers(httpHeaders -> {
                httpHeaders.set(MOCK_AUTH_TOKEN_HEADER, mockAuthToken);
              })
              .build();

          return next.exchange(newReq)
              .doFinally(sigType -> MDC.clear());
        }));
    client = new StructuredMonoWebClient(clientBuilder);
  }

  /**
   * "middleBiz" mock 서비스를 호출하는 외부 요청 로직입니다.
   *
   * <p>요청 URI는 {@code /mock/middleBiz}이며, 성공 응답은 INFO 로그로 기록되고,
   * HTTP 오류 발생 시 WARN 로그를 출력하고 fallback 문자열을 반환합니다.
   * </p>
   *
   * @return 외부 서비스 응답 문자열 또는 오류 fallback 문자열
   */
  @Override
  public String externalMiddleCall() {
    logger.info(LogType.BIZ, "External Middle Call 서비스 시작.");
    try {
      return invokeExternalCall(HttpMethod.POST.name(), "/middleBiz", "[I Called MiddleBiz]")
          .block();
    } catch (Exception e) {
      logger.error(LogType.BIZ, "Exception during externalMiddleCall", e);
      return "EXCEPTION: " + e.getMessage();
    }
  }

  /**
   * "simpleBiz" mock 서비스를 호출하는 외부 요청 로직입니다.
   *
   * <p>요청 URI는 {@code /mock/simpleBiz}이며, 성공 응답은 INFO 로그로 기록되고,
   * HTTP 오류 발생 시 WARN 로그를 출력하고 fallback 문자열을 반환합니다.
   * </p>
   *
   * @return 외부 서비스 응답 문자열 또는 오류 fallback 문자열
   */
  @Override
  public String externalSimpleCall() {
    logger.warn(LogType.BIZ, "External Simple Call 서비스 시작.", new IllegalStateException("테스트용"));
    try {
      return invokeExternalCall(HttpMethod.GET.name(), "/simpleBiz", "[I Called simpleBiz]")
          .block();
    } catch (Exception e) {
      logger.error(LogType.BIZ, "Exception during externalMiddleCall", e);
      return "EXCEPTION: " + e.getMessage();
    }
  }

  private Mono<String> invokeExternalCall(String method, String uri, String body) {
    Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
    return client.executeMonoCall(method, uri, body, String.class)
        .map(
          WebClientCallResult::getData
        ).onErrorResume(ex -> {
          MDC.setContextMap(mdcSnapshot);
          logger.warn(LogType.BIZ, "External call failed ({} {}): {}", method, uri, ex.toString());
          return Mono.just("ERROR: " + ex.getMessage());
        }
    );

  }

}
