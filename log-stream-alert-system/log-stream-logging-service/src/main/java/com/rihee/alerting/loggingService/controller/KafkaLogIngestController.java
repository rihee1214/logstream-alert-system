package com.rihee.alerting.loggingService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihee.alerting.loggingService.service.LogIngestService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
public class KafkaLogIngestController {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE
                                          = new TypeReference<Map<String, Object>>() {};
  private final Logger logger = LoggerFactory.getLogger(KafkaLogIngestController.class);
  @Autowired
  private LogIngestService logIngestServiceImpl;

  @KafkaListener(topics = "${kafka.log.ingest.topic}")
  public void receive(String message) {
    try {
      Map<String, Object> node = MAPPER.readValue(message, MAP_TYPE_REFERENCE);
      logIngestServiceImpl.processingLog(node);
    } catch (JsonProcessingException e) {
      logger.warn("대상 로그를 파싱할 수 없습니다. : {}", message);
    }

  }

}
