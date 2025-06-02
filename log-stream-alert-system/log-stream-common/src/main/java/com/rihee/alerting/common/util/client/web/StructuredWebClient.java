package com.rihee.alerting.common.util.client.web;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class StructuredWebClient {

  protected final WebClient wc;

  public StructuredWebClient() {
    this(WebClient.builder());
  }

  public StructuredWebClient(WebClient.Builder builder) {
    wc = builder.build();
//    wc.get().uri("").exchangeToMono(clientResponse -> {
//      clientResponse.headers()
//    })
  }



}
