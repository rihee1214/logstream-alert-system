package com.rihee.alerting.mockservice.controller;

import com.rihee.alerting.common.annotation.StructuredGetMapping;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import com.rihee.alerting.mockservice.service.MockBizService;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code MockBizController}는 mock-service의 단순 비즈니스 호출을 위한 REST API 엔드포인트를 제공합니다.
 *
 * <p>비즈니스 처리 흐름을 단순히 시작하고 종료하는 API로써,
 * {@link MockBizService}의 로직을 호출하며 structured logging 기능을 테스트하거나 연동할 수 있는 목적을 갖습니다.
 *
 * <p>Structured 로그 출력은 {@link StructuredLogger}를 통해 수행됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@RestController
public class MockBizController {

  /**
   * 구조화 로그 출력을 위한 {@link StructuredLogger} 인스턴스입니다.
   */
  private static final StructuredLogger logger
                = StructuredLoggerFactory.getLogger(MockBizController.class);
  /**
   * 비즈니스 로직을 위임 실행할 서비스 클래스입니다.
   */
  private final MockBizService mockBizServiceImpl;

  /**
   * {@code MockBizController}의 생성자입니다.
   *
   * @param mockBizServiceImpl 비즈니스 로직 처리를 위한 서비스 구현체
   */
  public MockBizController(MockBizService mockBizServiceImpl) {
    this.mockBizServiceImpl = mockBizServiceImpl;
  }

  /**
   * 간단한 mock 비즈니스 호출을 처리하는 GET 엔드포인트입니다.
   *
   * <p>요청을 수신하면 {@link StructuredLogger}를 통해 structured 로그를 출력하고,
   * 내부 비즈니스 로직({@link MockBizService})을 실행한 후 고정된 메시지를 반환합니다.
   *
   * <p>이 메서드는 {@link com.rihee.alerting.common.annotation.StructuredGetMapping}을 사용하며,
   * 지정된 {@code spanLabel}을 기반으로 공통 모듈의 {@code StructuredLogInterceptor}가 동작하여
   * 요청 흐름에 대한 trace context 정보를 설정합니다.
   *
   * <p>구체적인 trace context 설정 방식은 공통 모듈의 로깅 아키텍처 문서를 참고하세요:
   * {@code /docs/guide/development/common/logging-interceptor.md}
   *
   * @return "simpleBizMockup Call End" 고정 응답 문자열
   */
  @StructuredGetMapping(value = "/simpleBiz", spanLabel = "simpleBiz")
  public String simpleBizMockupService() {
    logger.info(LogType.BIZ, "simpleBizMockup 서비스를 시작합니다.");
    mockBizServiceImpl.doSomething();
    return "simpleBizMockup Call End";
  }
}
