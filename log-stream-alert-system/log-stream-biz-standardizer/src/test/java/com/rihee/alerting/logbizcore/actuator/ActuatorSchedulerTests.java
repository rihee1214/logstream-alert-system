package com.rihee.alerting.logbizcore.actuator;

import static com.rihee.alerting.common.constant.observability.CallCommonFields.ELAPSED_MS;
import static com.rihee.alerting.common.constant.observability.CallCommonFields.TYPE;
import static com.rihee.alerting.common.constant.observability.HttpCallFields.METHOD;
import static com.rihee.alerting.common.constant.observability.HttpCallFields.STATUS_CODE;
import static com.rihee.alerting.common.constant.observability.HttpCallFields.STATUS_MESSAGE;
import static com.rihee.alerting.common.constant.observability.HttpCallFields.URI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihee.alerting.logbizcore.LogBizCoreTestBootstrap;
import com.rihee.alerting.logbizcore.log.appender.MemoryAppender;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * {@code ActuatorSchedulerTests}는 {@link ActuatorHealthMonitoringScheduler} 클래스의 동작을
 * 검증하는 테스트 클래스입니다.
 *
 * <p>이 테스트는 Spring Boot 애플리케이션이 기동된 상태에서
 * /actuator/health endpoint를 호출하여 structured log가 올바르게 생성되는지를 확인합니다.
 *
 * <p>로그는 커스텀 메모리 기반 Appender인 {@link MemoryAppender}를 통해 수집되며,
 * 로그 출력 내에 포함된 JSON 문자열을 파싱하여 메타 필드와 HTTP 상태 코드를 검증합니다.
 *
 * <p>테스트는 {@code test} profile로 실행되며, {@link LogBizCoreTestBootstrap}을 통해 공통 설정이 로딩됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT,
                classes = {LogBizCoreTestBootstrap.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.profiles.active=dev")
public class ActuatorSchedulerTests {

  /**
   * 테스트 대상인 스케줄러 컴포넌트로, actuator health endpoint를 호출하여 로그를 출력합니다.
   */
  @Autowired
  private ActuatorHealthMonitoringScheduler scheduler;

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
   * 이를 통해 테스트 수행 중 출력되는 로그를 수집할 수 있습니다.
   */
  @BeforeEach
  void setUp() {
    memoryAppender = new MemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    memoryAppender.start();

    rootLogger.addAppender(memoryAppender);
  }

  /**
   * 테스트 종료 후 MemoryAppender를 정지시키고 루트 로거에서 제거하여 자원 누수와 충돌을 방지합니다.
   */
  @AfterEach
  void tearDown() {
    memoryAppender.stop();
    rootLogger.detachAppender(memoryAppender);
  }

  /**
   * {@code scheduleActuatorLogs()} 실행 시 actuator health endpoint로 요청이 전송되고,
   * 그 결과로 structured log가 남는지 검증합니다.
   *
   * <p>로그는 "status": "UP" 문자열을 포함한 이벤트를 기준으로 필터링되며,
   * 응답 내 포함된 meta 필드의 HTTP 상태 코드가 200인지 확인합니다.
   *
   * <p>실제 로그 메시지는 JSON으로 구성되며 {@link ObjectMapper}를 통해 파싱됩니다.
   */
  @Test
  void actuatorHealthCallShouldLogSuccess() {
    // 1. 스케줄러 수동 실행
    scheduler.scheduleActuatorLogs();

    // 2. 로그 필터링 및 존재 여부 검증
    Optional<ILoggingEvent> log = memoryAppender.getLoggedEvents().stream()
        .filter(e -> e.getFormattedMessage().contains("\"status\":\"UP\""))
        .findFirst();

    // 로그가 존재하지 않으면 테스트 실패
    assertThat(log).isPresent();

    String logJson = log.map(ILoggingEvent::getFormattedMessage).orElse(null);

    assertThat(logJson).as("status가 UP인 로그 메시지가 존재해야합니다.")
                      .isNotNull();

    // 3. JSON 파싱
    Map<String, String> mdcMap = log.map(ILoggingEvent::getMDCPropertyMap).orElse(new HashMap<>());
    Map<String, String> reqMap = Map.of(
        TYPE.getFieldName(), mdcMap.get(TYPE.getFieldName()),
        ELAPSED_MS.getFieldName(), mdcMap.get(ELAPSED_MS.getFieldName()),
        METHOD.getFieldName(), mdcMap.get(METHOD.getFieldName()),
        STATUS_CODE.getFieldName(), mdcMap.get(STATUS_CODE.getFieldName()),
        STATUS_MESSAGE.getFieldName(), mdcMap.get(STATUS_MESSAGE.getFieldName()),
        URI.getFieldName(), mdcMap.get(URI.getFieldName())
    );

    for (String key : reqMap.keySet()) {
      assertThat(reqMap.get(key)).as(key + "| 의 값은 비어있으면 안됩니다.").isNotEmpty();
    }

    int statusCode = Integer.parseInt(reqMap.get(STATUS_CODE.getFieldName()));
    assertThat(statusCode).as("HTTP 상태 코드는 200이어야 합니다.")
                          .isEqualTo(200);
  }
}
