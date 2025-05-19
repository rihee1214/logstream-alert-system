package com.rihee.alerting.mockservice.simpleCall;

import static com.rihee.alerting.common.log.constant.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.TRACE_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.rihee.alerting.common.config.CommonInterceptorConfiguration;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.mockservice.configuration.MockHttpServletRequestConfig;
import com.rihee.alerting.mockservice.log.MemoryAppender;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({MockHttpServletRequestConfig.class, CommonInterceptorConfiguration.class})
public class SimpleMockServiceTests {

  private static final String MOCK_AUTH_TOKEN_HEADER = "X-Auth-Token";

  private static final StructuredLogger log
      = StructuredLoggerFactory.getLogger(SimpleMockServiceTests.class);

  /**
   * Spring의 {@code MockMvc}를 주입 받아 실제 요청처럼 테스트 메서드를 수행하기 위한 목적으로 사용됩니다.
   */
  @Autowired
  private MockMvc mockMvc;
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
  public void simpleMockupCallTest() throws Exception {
    // 첫 번째 요청으로 traceId, spanId가 잘 생성되어있는지 확인
    mockMvc.perform(get("/simpleBiz").header(MOCK_AUTH_TOKEN_HEADER, "test-token"))
        .andExpect(status().isOk());

    List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

    assertThat(events.size()).isEqualTo(3);

    List<String> tranIds = new ArrayList<>();
    List<String> spanIds = new ArrayList<>();
    List<String> parentSpanIds = new ArrayList<>();

    for (ILoggingEvent event : events) {
      Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
      String tranId = mdcPropertyMap.get(TRACE_ID.getName());
      String spanId = mdcPropertyMap.get(SPAN_ID.getName());
      String parentSpanId = mdcPropertyMap.get(PARENT_SPAN_ID.getName());

      assertThat(tranId).isNotEmpty();
      assertThat(spanId).isNotEmpty();
      assertThat(parentSpanId).isNullOrEmpty();

      tranIds.add(tranId);
      spanIds.add(spanId);
      parentSpanIds.add(parentSpanId);
    }

    assertThat(new HashSet<>(tranIds).size()).isEqualTo(1);
    assertThat(new HashSet<>(spanIds).size()).isEqualTo(1);
    assertThat(new HashSet<>(parentSpanIds).size()).isEqualTo(1);
  }
}
