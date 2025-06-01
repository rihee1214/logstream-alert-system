package com.rihee.alerting.common.util.client.web;

import com.rihee.alerting.common.util.client.web.builder.TraceableWebClientBuilderFactory;
import org.springframework.web.reactive.function.client.WebClient;

public class StructuredWebClient {

  protected final WebClient wc;


  public StructuredWebClient() {
    this(TraceableWebClientBuilderFactory.makeNewTraceableWebClientBuilder());
  }

  public StructuredWebClient(WebClient.Builder builder) {
    wc = builder.build();
  }


}
