package com.rihee.alerting.common.util;

import static com.rihee.alerting.common.log.constant.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.TRACE_ID;

import com.rihee.alerting.common.constant.B3Header;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@code TraceableWebClientBuilderFactory}는 WebClient 사용 시 traceId, spanId, parentSpanId를
 * 자동으로 B3 헤더에 포함시키는 공통 정책 필터를 제공하는 유틸리티 클래스입니다.
 *
 * <p>이 클래스는 시스템의 구조화 로깅 및 추적 일관성을 보장하기 위해 사용되며,
 * Biz 서비스 개발자는 이 클래스에서 생성한 {@link WebClient.Builder} 위에
 * baseUrl, 인증 헤더, 기타 커스터마이징을 자유롭게 추가할 수 있습니다.
 *
 * <p><b>주의:</b> 이 필터는 공통 정책이며, 재정의 또는 수정이 불가능합니다.
 * Biz 서비스는 이 위에 추가적인 필터를 <b>덧붙이는 방식</b>으로 확장해야 합니다.
 */
public final class TraceableWebClientBuilderFactory {

  private TraceableWebClientBuilderFactory() {

  }

  /**
   * traceId, spanId, parentSpanId를 자동으로 주입하는 필터가 포함된
   * {@link WebClient.Builder}를 생성합니다.
   *
   * <p>해당 빌더는 다음과 같은 동작을 기본으로 수행합니다:
   * <ul>
   *   <li>{@code MDC}로부터 {@code traceId}, {@code spanId}, {@code parentSpanId} 값을 가져옴</li>
   *   <li>B3 Header 규격에 따라 각각 {@code X-B3-TraceId},
   *   {@code X-B3-SpanId}, {@code X-B3-ParentSpanId}에 주입</li>
   *   <li>요청 처리 중 MDC 컨텍스트를 보존하고, 응답 완료 시 MDC를 정리</li>
   * </ul>
   *
   * <p>서비스에서는 이 빌더에 {@code .baseUrl()}, {@code .defaultHeader()}, {@code .filter()} 등을 추가하여
   * 필요한 설정을 덧붙이면 됩니다.
   *
   * @return 공통 추적 필터가 포함된 {@link WebClient.Builder}
   */
  public static WebClient.Builder makeNewTraceableWebClientBuilder() {
    return WebClient.builder()
              .filter(((request, next) -> {
                Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
                if (mdcSnapshot == null) {
                  throw new IllegalStateException(
                      "MDC context is missing. "
                          + "This WebClient must be invoked within a structured logging context "
                          + "(e.g., via controller or scheduled task)."
                  );
                }
                // traceId, spanId, parentSpanId를 추출하여 B3 헤더에 주입
                ClientRequest.Builder clientBuilder = ClientRequest.from(request);

                String traceId = mdcSnapshot.get(TRACE_ID.getName());
                String spanId = mdcSnapshot.get(SPAN_ID.getName());
                String parentSpanId = mdcSnapshot.get(PARENT_SPAN_ID.getName());

                clientBuilder.header(B3Header.TRACE_ID.getHeaderName(), traceId);
                clientBuilder.header(B3Header.SPAN_ID.getHeaderName(), spanId);
                if (parentSpanId != null) {
                  clientBuilder.header(B3Header.PARENT_SPAN_ID.getHeaderName(), parentSpanId);
                }

                ClientRequest newRequest = clientBuilder.build();

                return next.exchange(newRequest);
              }));
  }

}
