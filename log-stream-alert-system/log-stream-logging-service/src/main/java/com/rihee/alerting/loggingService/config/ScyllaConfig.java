package com.rihee.alerting.loggingService.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScyllaConfig {

  @Bean
  public CqlSession cqlSession(){

    
    return CqlSession.builder()

            .build();
  }
}
