package com.rihee.alerting.mockservice.controller;

import com.rihee.alerting.logbizcore.annotation.StructuredGetMapping;
import com.rihee.alerting.logbizcore.annotation.StructuredPostMapping;
import com.rihee.alerting.logbizcore.log.StructuredLogger;
import com.rihee.alerting.logbizcore.log.StructuredLoggerFactory;
import com.rihee.alerting.common.constant.log.LogType;
import com.rihee.alerting.mockservice.service.MockBizService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code MockBizController}는 mock 시스템의 비즈니스 흐름을 테스트하기 위한
 * 진입점 역할을 담당하는 컨트롤러입니다.
 *
 * <p>요청 흐름은 {@code MockBizService}를 통해 실제 외부 호출이나 내부 데이터 검색 등의
 * 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>⚠️ 실패 처리 주의사항:</strong></p>
 *
 * <p>현재 구현은 mock 테스트 흐름을 구성하기 위한 기본 버전이며,
 * 실패 상황 (예: 외부 호출 실패, 예외 발생, timeout 등)에 대한
 * 구체적인 예외 처리 및 복구 로직은 아직 포함되어 있지 않습니다.
 * </p>
 *
 * <p>향후 확장 시 다음 항목을 고려하여 보완 예정입니다:</p>
 * <ul>
 *   <li>예외 발생 시 fallback 처리</li>
 *   <li>구간별 실패 상태 로깅 및 모니터링</li>
 *   <li>통합 에러 응답 포맷</li>
 * </ul>
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
   * "Simple Call" 시나리오에서 단일 mock 서비스를 실행합니다.
   *
   * <p>외부 호출 없이 단일 흐름만을 검증하며, {@code doSimpleSomething()} 호출을 통해
   * 내부 DB 조회 또는 간단한 외부 의존성을 테스트합니다.
   * </p>
   *
   * @return "simpleBizMockup Call End" 응답 문자열
   */
  @StructuredGetMapping(value = "/simpleBiz", spanLabel = "simpleBiz")
  public String simpleBizMockupService() {
    logger.info(LogType.BIZ, "simpleBizMockup 서비스를 시작합니다.");
    String response = mockBizServiceImpl.doSimpleSomething();
    logger.info(LogType.BIZ, "simpleBizService로 부터 응답을 받았습니다. : {}", response);
    return "simpleBizMockup Call End";
  }

  /**
   * "Branch Call" 및 "Multi Layer Call" 시나리오에서 중간 단계로 사용되는 mock 서비스입니다.
   *
   * <p>이전 단계(mock)로부터 호출되며, 내부적으로 {@code doMiddleSomething()}을 실행합니다.
   * 외부 시스템 호출을 포함합니다.
   * </p>
   *
   * @return "middleBizMockup Call End" 응답 문자열
   */
  @StructuredPostMapping(value = "/middleBiz", spanLabel = "middleBiz")
  public String middleBizMockupService(@RequestBody String parameter) {
    logger.info(LogType.BIZ, "middleBizMockup 서비스를 시작합니다. : {}", parameter);
    String response = mockBizServiceImpl.doMiddleSomething();
    logger.info(LogType.BIZ, "middleBizService로 부터 응답을 받았습니다. : {}", response);
    return "middleBizMockup Call End";
  }

  /**
   * "Branch Call" 시나리오의 시작점으로, 병렬 호출 테스트를 위한 mock 서비스입니다.
   *
   * <p>요청 시 여러 mock 서비스를 병렬로 호출하거나, 그 흐름을 분기시켜
   * 복수의 응답 및 흐름 테스트 시나리오를 검증할 수 있습니다.
   * </p>
   *
   * @return "branchBizMockup Call End" 응답 문자열
   */
  @StructuredGetMapping(value = "/branchBiz", spanLabel = "branchBiz")
  public String branchBizMockupService() {
    logger.info(LogType.BIZ, "branchBizMockup 서비스를 시작합니다.");
    String response = mockBizServiceImpl.doBranchSomething();
    logger.info(LogType.BIZ, "branchBizService로 부터 응답을 받았습니다. : {}", response);
    return "branchBizMockup Call End";
  }

  /**
   * "Multi Layer Call" 시나리오의 진입점으로, 연속된 mock 서비스 호출을 구성합니다.
   *
   * <p>요청을 수신하면 중간 mock 서비스(`middleBiz`)와 최종 mock(`simpleBiz`)로
   * 순차 호출을 수행하는 구조를 기반으로 흐름을 시뮬레이션합니다.
   * </p>
   *
   * @return "multiLayerBizMockup Call End" 응답 문자열
   */
  @StructuredGetMapping(value = "/multiLayerBiz", spanLabel = "multiLayerBiz")
  public String multiLayerBizMockupService() {
    logger.info(LogType.BIZ, "multiLayerBizMockup 서비스를 시작합니다.");
    String response = mockBizServiceImpl.doMultiLayerSomething();
    logger.info(LogType.BIZ, "multiLayerService로 부터 응답을 받았습니다. : {}", response);
    return "multiLayerBizMockup Call End";
  }
}
