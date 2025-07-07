package com.rihee.alerting.loggingService.controller;

import org.springframework.kafka.annotation.KafkaListener;

public class LogIngestController {

  @KafkaListener(topics = "${kafka.log.ingest.topic}")
  public void receive(String message) {
    
  }

}
