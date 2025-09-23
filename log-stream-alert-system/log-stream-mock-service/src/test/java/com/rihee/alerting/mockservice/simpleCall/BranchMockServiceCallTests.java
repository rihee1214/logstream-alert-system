package com.rihee.alerting.mockservice.simpleCall;

import static com.rihee.alerting.common.constant.logging.StructuredLogFields.LOG_TYPE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.PARENT_SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.TRACE_ID;
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
import com.rihee.alerting.common.constant.logging.LogType;
import com.rihee.alerting.mockservice.configuration.MockHttpServletRequestConfig;
import com.rihee.alerting.mockservice.log.MemoryAppender;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code BranchMockServiceCallTests}는 mock-service 모듈의 핵심 비즈니스 흐름을 따라가며
 * 로그 기반 트레이스 ID 전파, Span 계층 구조가 정상적으로 기록되는지를 검증하는 통합 테스트입니다.
 *
 * <p>시나리오는 다음과 같은 호출 흐름을 기반으로 합니다:
 * <pre>
 * 1. branchBiz 진입
 * 2. branchBiz → middleBiz → simpleBiz (재귀적 외부 호출)
 * 3. 이후 branchBiz가 직접 simpleBiz를 다시 호출 (분기 재진입)
 * </pre>
 *
 * <p>각 호출에서 기록되는 로그는 {@code MemoryAppender}를 통해 실시간 수집되며,
 * 다음 항목들을 검증합니다:
 * <ul>
 *   <li>단일 traceId의 일관성 유지 여부</li>
 *   <li>spanId → parentSpanId로 연결되는 계층적 호출 구조</li>
 *   <li>총 로그 수 (26개)가 정확히 발생했는지</li>
 * </ul>
 *
 * @author 리희
 * @see StructuredLogger
 * @see MemoryAppender
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Import({MockHttpServletRequestConfig.class, CommonInterceptorConfiguration.class})
public class BranchMockServiceCallTests {

  /**
   * 테스트 로그 출력을 위한 StructuredLogger 인스턴스입니다.
   */
  private static final StructuredLogger log
      = StructuredLoggerFactory.getLogger(BranchMockServiceCallTests.class);

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
   * 테스트 대상 URL에 HTTP 요청을 전송하여 다음을 검증합니다:
   * <ol>
   *   <li>모든 로그에서 traceId가 동일하게 유지됨</li>
   *   <li>branch → middle → simple 흐름이 올바른 parentSpanId 계층으로 구성됨</li>
   *   <li>branch → simple 재호출 시에도 적절한 parentSpanId 연결 확인</li>
   * </ol>
   *
   * <p>전체 로그는 시간순으로 정렬되며, MDC에서 필드 추출 후 assert로 검증됩니다.
   *
   * @throws Exception HTTP 요청 수행 중 예외 발생 시
   */
  @Test
  public void branchMockupCallTest() throws Exception {
    // 첫 번째 요청으로 traceId, spanId가 잘 생성되어있는지 확인
    mockMvc.perform(get("/branchBiz").header(MOCK_AUTH_TOKEN_HEADER.getHeaderName(), "test-token"))
        .andExpect(status().isOk());

    List<ILoggingEvent> events
        = memoryAppender.getLoggedEvents()
                        .stream()
                        .filter(event -> LogType.BIZ.getCode()
                            .equals(event.getMDCPropertyMap().get(LOG_TYPE.getFieldName()))
                        )
                        .sorted(Comparator.comparing(ILoggingEvent::getTimeStamp))
                        .toList();

    assertThat(events.size()).isEqualTo(26);

    Map<String, String> event1 = events.getFirst().getMDCPropertyMap();
    Map<String, String> event2 = events.get(3).getMDCPropertyMap();
    Map<String, String> event3 = events.get(6).getMDCPropertyMap();
    Map<String, String> event4 = events.get(17).getMDCPropertyMap();

    // 전체의 TraceId 동일성 검증
    assertThat(event1.get(TRACE_ID.getFieldName())).isEqualTo(event2.get(TRACE_ID.getFieldName()));
    assertThat(event1.get(TRACE_ID.getFieldName())).isEqualTo(event3.get(TRACE_ID.getFieldName()));
    assertThat(event1.get(TRACE_ID.getFieldName())).isEqualTo(event4.get(TRACE_ID.getFieldName()));

    // 전체의 부모 spanId와 자식의 parentSpanId 정합성 검증
    assertThat(event1.get(SPAN_ID.getFieldName())).isEqualTo(event2.get(PARENT_SPAN_ID.getFieldName()));
    assertThat(event2.get(SPAN_ID.getFieldName())).isEqualTo(event3.get(PARENT_SPAN_ID.getFieldName()));
    assertThat(event1.get(SPAN_ID.getFieldName())).isEqualTo(event4.get(PARENT_SPAN_ID.getFieldName()));
  }

  /* ▼ 호출 흐름 요약 (Stack 구조 기준)
    FIRST PUSH
    1.  branch C  -- FIRST BRANCH
    2.      branch S
    3.          branch I → call middleBiz
    4.              middle C  -- FIRST MIDDLE
    5.                  middle S
    6.                      middle I → call simpleBiz
    7.                          simple C  -- FIRST SIMPLE CALL BY MIDDLE
    8.                              simple S
    9.                                  simple D

    POP
    10.                                 ⬅ simple D
    11.                             ⬅ simple S
    12.                         ⬅ simple C
    13.                     ⬅ middle I
    14.                 ⬅ middle S
    15.             ⬅ middle C
    16.         ⬅ branch I

    RE-ENTRY
    17.         branch I
    18.             simple C  -- FIRST SIMPLE CALL BY BRANCH
    19.                 simple S
    20.                     simple D

    FINAL POP
    21.                     ⬅ simple D
    22.                 ⬅ simple S
    23.             ⬅ simple C
    24.         ⬅ branch I
    25.     ⬅ branch S
    26. ⬅ branch C
   */
}
