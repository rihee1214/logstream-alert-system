package com.rihee.alerting.common.config;

import com.rihee.alerting.common.actuator.handler.ActuatorCallLoggingHandler;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorCallHandlerConfig {

  @Bean
  public List<ActuatorCallLoggingHandler> actuatorCallLoggingHandlers(
      ObjectProvider<ActuatorCallLoggingHandler> provider
  ) {
    return provider.orderedStream().toList();
  }
}
