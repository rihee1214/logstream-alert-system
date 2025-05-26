package com.rihee.alerting.common.util;

import static com.rihee.alerting.common.log.constant.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.TRACE_ID;

import com.rihee.alerting.common.constant.B3Header;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

public final class TraceableWebClientBuilderFactory {

  private TraceableWebClientBuilderFactory(){}

  public WebClient.Builder makeNewTraceableWebClientBuilder() {
    return WebClient.builder()
              .filter(((request, next) -> {
                Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
                // traceId, spanId, parentSpanId를 추출하여 B3 헤더에 주입
                ClientRequest.Builder clientBuilder = ClientRequest.from(request);
                if (mdcSnapshot != null) {
                  String traceId = mdcSnapshot.get(TRACE_ID.getName());
                  String spanId = mdcSnapshot.get(SPAN_ID.getName());
                  String parentSpanId = mdcSnapshot.get(PARENT_SPAN_ID.getName());

                  clientBuilder.header(B3Header.TRACE_ID.getHeaderName(), traceId);
                  clientBuilder.header(B3Header.SPAN_ID.getHeaderName(), spanId);
                  if (parentSpanId != null) {
                    clientBuilder.header(B3Header.PARENT_SPAN_ID.getHeaderName(), parentSpanId);
                  }
                }

                ClientRequest newRequest = clientBuilder.build();

                return next.exchange(newRequest)
                    .doOnEach(signal -> {
                      MDC.setContextMap(mdcSnapshot);
                    })
                    .doFinally(signalType -> {
                      MDC.clear();
                    });
              }));
  }

}
