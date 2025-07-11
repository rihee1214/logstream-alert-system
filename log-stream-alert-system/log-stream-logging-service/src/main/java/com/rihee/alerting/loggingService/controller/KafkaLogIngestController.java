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

/**
 * {@code KafkaLogIngestController}는 Kafka를 통해 수신된 로그 메시지를 처리하는 컨트롤러입니다.
 *
 * <p>Kafka로부터 수신된 로그 메시지(String 형태의 JSON)를 {@link ObjectMapper}를 사용하여
 * {@code Map<String, Object>} 형식으로 역직렬화하고, 이를 {@link LogIngestService}를 통해 처리합니다.
 *
 * <p>파싱 실패 시, 원본 메시지를 WARN 레벨로 로깅하며 예외를 무시합니다.
 *
 * <p>이 컨트롤러는 Spring Kafka의 {@link KafkaListener} 어노테이션을 통해 로그 메시지를 자동 수신하며,
 * 실제 서비스 로직은 주입된 {@link LogIngestService}에서 수행합니다.
 *
 * @author 리희
 * @since 1.0
 */
@Controller
public class KafkaLogIngestController {

  /**
   * Kafka 메시지를 역직렬화하기 위한 Jackson의 {@link ObjectMapper}.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();
  /**
   * Kafka 메시지를 파싱할 때 사용할 {@code Map<String, Object>} 타입 참조 객체.
   */
  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE
                                          = new TypeReference<Map<String, Object>>() {};
  private final Logger logger = LoggerFactory.getLogger(KafkaLogIngestController.class);
  @Autowired
  private LogIngestService logIngestServiceImpl;

  /**
   * Kafka로부터 로그 메시지를 수신하는 메서드.
   *
   * <p>수신된 메시지는 JSON 형식이어야 하며, 내부적으로 {@code Map<String, Object>} 형태로 변환되어
   * {@link LogIngestService#processingLog(Map)}를 통해 처리됩니다.
   *
   * <p>만약 JSON 파싱에 실패할 경우, 해당 메시지를 로깅한 뒤 처리를 중단합니다.
   *
   * @param message Kafka로부터 수신된 JSON 문자열 메시지
   */
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
