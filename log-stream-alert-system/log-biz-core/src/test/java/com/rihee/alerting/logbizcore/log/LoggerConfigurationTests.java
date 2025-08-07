package com.rihee.alerting.logbizcore.log;

import ch.qos.logback.classic.Logger;
import com.rihee.alerting.logbizcore.log.appender.MemoryAppender;
import com.rihee.alerting.common.constant.message.LogType;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 로그 설정 및 로깅 기능 검증 테스트.
 */
@SpringBootTest
public class LoggerConfigurationTests {

  private static final StructuredLogger log = StructuredLoggerFactory.getLogger(
      LoggerConfigurationTests.class);
  private final MemoryAppender memoryAppender;

  /**
   * MemoryAppender를 Root Logger에 추가하여 테스트 중 발생하는 모든 로그를 수집한다.
   */
  public LoggerConfigurationTests() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    memoryAppender = new MemoryAppender();
    memoryAppender.setContext(rootLogger.getLoggerContext());
    memoryAppender.start();

    rootLogger.addAppender(memoryAppender);
  }

  /**
   * BIZ, SYS 로그가 올바르게 찍히는지 검증한다. - 각 로그에 "logtype" 키가 포함되어 있는지 - 로그 레벨이 "INFO"로 출력되는지
   */
  @Test
  void printLogsAccordingToLogbackConfiguration() {
    MDC.put("service", "log-test");
    MDC.put("host", "tester");
    MDC.put("container", "tester");

    log.info(LogType.SYS, "sys-test-message");

    Assertions.assertAll(
        () -> memoryAppender.getLoggedEvents().forEach(event -> {
          String formattedMessage = event.getFormattedMessage();

          Assertions.assertDoesNotThrow(() -> new JSONObject(formattedMessage));
          Assertions.assertTrue(formattedMessage.contains("\"logtype\""), "logtype이 누락되었습니다.");
          Assertions.assertTrue(formattedMessage.contains("\"level\":\"INFO\""),
              "level이 INFO가 아닙니다.");
        })
    );

  }
}
