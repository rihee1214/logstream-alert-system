package com.rihee.alerting.common.log;

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
import com.rihee.alerting.common.configuration.MockHttpServletRequestConfig;
import com.rihee.alerting.common.constant.B3Header;
import com.rihee.alerting.common.log.appender.MemoryAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

/**
 * {@code StructuredLoggingInterceptorTests}는 로그 필드 자동 세팅 기능(MDC 설정 로직)을 검증하기 위한 통합 테스트 클래스입니다.
 *
 * <p>해당 테스트는 커스텀 애너테이션 기반으로 동작하는 {@code MDCInterceptor} 및 {@code MDCHandlerMethodPostProcessor}의
 * 로직이 의도대로 작동하여 {@code traceId}, {@code spanId}, {@code parentSpanId}와 같은 MDC 필드가
 * 정확히 로그에 포함되는지를 확인합니다.</p>
 *
 * <p>특히 {@code @StructuredPostMapping} 애너테이션을 통한 요청 흐름에서
 * 인터셉터가 어떻게 MDC 필드를 구성하고, 로그 라우팅까지 반영되는지 통합적으로 확인합니다.
 *
 * <p>모든 로그는 {@code MemoryAppender}를 통해 in-memory로 수집되며, 로그 필드 포함 여부는 Assert 구문으로 검증됩니다.</p>
 */
@SpringBootTest(properties = "spring.profiles.active=dev")
@AutoConfigureMockMvc
@Import({MockHttpServletRequestConfig.class, CommonInterceptorConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StructuredLoggingInterceptorTests {

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

  /**
   * {@code @EnableStructuredLogging} 애너테이션 기반 설정 로직이
   * {@code traceId}, {@code spanId}, {@code parentSpanId} MDC 값을 로그 이벤트에 정확히 세팅하는지 검증합니다.
   *
   * <p>{@code /getMappingTestMockup} 엔드포인트를 호출하고, 로그 이벤트에서 해당 MDC 키들이 포함되었는지를 확인합니다.
   * {@code parentSpanId}는 설정되지 않아야 하므로 {@code false}를 기대합니다.
   *
   * @throws Exception 요청 처리 중 오류 발생 시
   */
  @Test
  void mockupServiceShouldSetMdcFields() throws Exception {
    // 첫 번째 요청으로 traceId, spanId가 잘 생성되어있는지 확인
    mockMvc.perform(get("/getMappingTestMockup"))
        .andExpect(status().isOk());

    ILoggingEvent event1 = memoryAppender.getLoggedEvents().getFirst();

    String traceId1 = event1.getMDCPropertyMap().get(TRACE_ID.getName());
    String spanId1 = event1.getMDCPropertyMap().get(SPAN_ID.getName());
    String parentSpanId1 = event1.getMDCPropertyMap().get(PARENT_SPAN_ID.getName());

    assertThat(StringUtils.hasText(traceId1)).isTrue();
    assertThat(StringUtils.hasText(spanId1)).isTrue();
    assertThat(StringUtils.hasText(parentSpanId1)).isFalse();

    memoryAppender.clear();

    // 두 번째 요청과 헤더 세팅으로, 로깅시 나오는 traceId, spanId, parentSpanId가 잘 생성되어있는지 확인
    mockMvc.perform(get("/getMappingTestMockup")
        .header(B3Header.TRACE_ID.getHeaderName(), traceId1)
        .header(B3Header.SPAN_ID.getHeaderName(), spanId1)
    ).andExpect(status().isOk());

    ILoggingEvent event2 = memoryAppender.getLoggedEvents().getFirst();

    String traceId2 = event2.getMDCPropertyMap().get(TRACE_ID.getName());
    String spanId2 = event2.getMDCPropertyMap().get(SPAN_ID.getName());
    String parentSpanId2 = event2.getMDCPropertyMap().get(PARENT_SPAN_ID.getName());

    assertThat(StringUtils.hasText(traceId2)).isTrue();
    assertThat(StringUtils.hasText(spanId2)).isTrue();
    assertThat(StringUtils.hasText(parentSpanId2)).isTrue();

    // 두 가지 요청을 보고 인터셉터가 제대로 된 동작을 하는지 확인
    assertThat(traceId1).isEqualTo(traceId2);
    assertThat(spanId1).isEqualTo(parentSpanId2);

    memoryAppender.clear();
  }

}
