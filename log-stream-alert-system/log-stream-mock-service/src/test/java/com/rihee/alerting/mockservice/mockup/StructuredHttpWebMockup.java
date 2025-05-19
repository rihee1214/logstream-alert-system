package com.rihee.alerting.mockservice.mockup;

import com.rihee.alerting.common.annotation.StructuredGetMapping;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code common} 모듈의 로그 구조 및 StructuredLogger 동작을 검증하기 위한 Mockup HTTP 컨트롤러입니다.
 *
 * <p>StructuredLogger와 {@code @StructuredGetMapping}, 그리고 관련 Interceptor/PreProcessor가
 * 요청에 따라 {@code traceId}, {@code spanId}, {@code parentSpanId}를 올바르게 생성 및 전파하는지 확인하는 용도로 사용됩니다.</p>
 *
 * <p>Mockup 환경 또는 통합 테스트 환경에서 이 컨트롤러를 호출함으로써,
 * MDC 기반 로깅 필드의 자동 주입 및 요청-응답 간 구조화 추적이 정상적으로 수행되는지를 검증할 수 있습니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
@RestController
public class StructuredHttpWebMockup {

  private static final StructuredLogger logger
                                = StructuredLoggerFactory.getLogger(StructuredHttpWebMockup.class);

  /**
   * 구조화된 로그 흐름과 Interceptor 기반 MDC 설정을 테스트하기 위한 Mock GET API입니다.
   *
   * <p>{@code @StructuredGetMapping} 애너테이션을 통해 사전 정의된 {@code spanLabel}이 적용되고,
   * Interceptor와 PreProcessor가 요청 시 {@code traceId}, {@code spanId}, {@code parentSpanId}를
   * 자동 생성 및 MDC에 주입하는 동작을 검증할 수 있습니다.</p>
   *
   * <p>이 엔드포인트를 호출하면 로그가 출력되며, 출력된 로그에서 필수 필드들이 올바르게 채워졌는지 확인할 수 있습니다.</p>
   *
   * @return 고정된 문자열 "mock response"
   */
  @StructuredGetMapping(spanLabel = "getMappingTest", value = "getMappingTestMockup")
  public String mockResponse() {
    logger.info(LogType.BIZ, "mockup service called");
    return "mock response";
  }
}
