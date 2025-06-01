package com.rihee.alerting.common.util.client.web.response;

import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class TraceableWebClientResponseBuilderFactory {

  private final WebClient wc;

  public TraceableWebClientResponseBuilderFactory(WebClient wc) {
    this.wc = wc;
  }

  public Mono<?> executeMono(String method, String uri) {
    return executeMono(HttpMethod.valueOf(method.toUpperCase()), uri);
  }

  public Mono<?> executeMono(HttpMethod method, String uri) {
    return wc.method(method).uri(uri).exchangeToMono(
      response -> {

        return Mono.empty();
      }
    );
  }
}
