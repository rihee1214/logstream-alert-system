package com.rihee.alerting.common.actuator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.rihee.alerting.common.log.appender.MemoryAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ActuatorSchedulerTests {

  @Autowired
  private CommonMonitoringScheduler scheduler;

  /**
   * 로그 출력을 메모리에 저장하여 테스트 중 로그 이벤트를 직접 검증할 수 있도록 지원하는 커스텀 Appender입니다.
   */
  private MemoryAppender memoryAppender;
  /**
   * 루트 로거에 메모리 Appender를 추가하여 모든 로그 이벤트를 테스트에서 수집할 수 있도록 설정합니다.
   */
  private final Logger rootLogger
      = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  /**
   * 테스트 실행 전 MemoryAppender를 초기화하고 루트 로거에 등록합니다.
   */
  @BeforeEach
  void setUp() {
    memoryAppender = new MemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    memoryAppender.start();

    rootLogger.addAppender(memoryAppender);
  }

  /**
   * 테스트 종료 후 MemoryAppender를 정지시키고 루트 로거에서 제거합니다.
   */
  @AfterEach
  void tearDown() {
    memoryAppender.stop();
    rootLogger.detachAppender(memoryAppender);
  }

  @Test
  void actuatorHealthCallShouldLogSuccess() {
    scheduler.scheduleActuatorLogs(); // 수동 호출

    var log = memoryAppender.getLoggedEvents().stream()
        .filter(e -> e.getFormattedMessage().contains("\"status\":\"UP\""))
        .findFirst();

    assertThat(log).isPresent();
  }
}
