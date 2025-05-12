package com.rihee.alerting.common.actuator.handler;

import java.util.Properties;
import org.springframework.web.reactive.function.client.WebClient;

public interface ActuatorCallLoggingHandler {

  void execute(WebClient client, Properties properties, String serviceName);
}
