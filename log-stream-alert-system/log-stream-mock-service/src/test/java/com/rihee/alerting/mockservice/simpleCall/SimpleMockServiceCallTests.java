package com.rihee.alerting.mockservice.simpleCall;

import static com.rihee.alerting.common.constant.log.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.constant.log.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.constant.log.StructuredLogProperties.TRACE_ID;
import static com.rihee.alerting.mockservice.constants.MockupHeaders.MOCK_AUTH_TOKEN_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.rihee.alerting.logbizcore.config.CommonInterceptorConfiguration;
import com.rihee.alerting.logbizcore.log.StructuredLogger;
import com.rihee.alerting.logbizcore.log.StructuredLoggerFactory;
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

/**
 * {@code SimpleMockServiceTests}는 mock-service 모듈의 단순 호출 테스트를 수행하며,
 * 인증 헤더 처리 및 구조화된 로그의 필수 필드(traceId, spanId, parentSpanId) 포함 여부를 검증합니다.
 *
 * <p>{@link MockMvc}를 사용하여 실제 HTTP 요청과 유사한 방식으로 API를 호출하며,
 * 테스트 중 출력된 로그는 {@link MemoryAppender}를 통해 메모리에 저장되어 검증됩니다.
 *
 * <p>테스트는 공통 StructuredLogger와 커스텀 로그 인터셉터가 정상 동작하는지를 확인하기 위해 사용됩니다.
 *
 * @see StructuredLogger
 * @see com.rihee.alerting.mockservice.log.MemoryAppender
 *
 * @author 리희
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({MockHttpServletRequestConfig.class, CommonInterceptorConfiguration.class})
public class SimpleMockServiceCallTests {

  /**
   * 테스트 로그 출력을 위한 StructuredLogger 인스턴스입니다.
   */
  private static final StructuredLogger log
      = StructuredLoggerFactory.getLogger(SimpleMockServiceCallTests.class);

  /**
   * {@code MockMvc}를 통해 HTTP 요청을 시뮬레이션합니다.
   */
  @Autowired
  private MockMvc mockMvc;
  /**
   * 테스트 중 출력되는 로그를 수집하기 위한 메모리 기반 로그 Appender입니다.
   */
  private MemoryAppender memoryAppender;
  /**
   * 루트 로거에 {@link MemoryAppender}를 동적으로 등록하기 위한 객체입니다.
   */
  private final Logger rootLogger
      = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  /**
   * 각 테스트 실행 전에 {@code MemoryAppender}를 초기화하고 루트 로거에 추가합니다.
   */
  @BeforeEach
  void setUp() {
    memoryAppender = new MemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    memoryAppender.start();

    rootLogger.addAppender(memoryAppender);
  }

  /**
   * 각 테스트 실행 후 {@code MemoryAppender}를 중지하고 루트 로거에서 제거합니다.
   */
  @AfterEach
  void tearDown() {
    memoryAppender.stop();
    rootLogger.detachAppender(memoryAppender);
  }

  /**
   * 단순 mock 서비스 API에 대해 토큰 기반 인증 요청을 수행하고, 그 결과가 200 OK임을 확인합니다.
   * 또한, 출력된 로그가 JSON 구조를 갖고 있으며 필수 필드(traceId, spanId, parentSpanId)가 포함되었는지를 검증합니다.
   *
   * @throws Exception HTTP 요청 수행 중 예외 발생 시
   */
  @Test
  public void simpleMockupCallTest() throws Exception {
    // 첫 번째 요청으로 traceId, spanId가 잘 생성되어있는지 확인
    mockMvc.perform(get("/simpleBiz").header(MOCK_AUTH_TOKEN_HEADER.getHeaderName(), "test-token"))
        .andExpect(status().isOk());

    List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

    assertThat(events.size()).isEqualTo(6);

    List<String> tranIds = new ArrayList<>();
    List<String> spanIds = new ArrayList<>();
    List<String> parentSpanIds = new ArrayList<>();

    for (ILoggingEvent event : events) {
      Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
      String tranId = mdcPropertyMap.get(TRACE_ID.getFieldName());
      String spanId = mdcPropertyMap.get(SPAN_ID.getFieldName());
      String parentSpanId = mdcPropertyMap.get(PARENT_SPAN_ID.getFieldName());

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
