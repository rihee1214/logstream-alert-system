package com.rihee.alerting.mockservice.service;

import com.rihee.alerting.logbizcore.log.StructuredLogger;
import com.rihee.alerting.logbizcore.log.StructuredLoggerFactory;
import com.rihee.alerting.common.constant.message.LogType;
import com.rihee.alerting.mockservice.dao.MockSearchDatabase;
import com.rihee.alerting.mockservice.infra.MockExternalCall;
import org.springframework.stereotype.Service;

/**
 * {@code MockBizServiceImpl}는 {@link MockBizService} 인터페이스의 구현체로,
 * 각 mock 테스트 시나리오(Single, Branch, Multi Layer)에 대응하는 실제 비즈니스 흐름을 처리합니다.
 *
 * <p>이 클래스는 구조화 로깅(StructuredLogger)을 기반으로 각 단계별 결과를 로그로 기록하며,
 * {@link MockSearchDatabase} 및 {@link MockExternalCall}을 통해 mock DB 조회와 외부 호출을 수행합니다.
 * </p>
 *
 * <p><strong>⚠️ 실패 처리 주의사항:</strong></p>
 *
 * <p>현재 구현은 정상 흐름 시나리오 테스트를 목적으로 하며,
 * 외부 호출 실패나 예외 발생에 대한 처리는 포함되어 있지 않습니다.<br>
 * 향후 아래 항목을 기반으로 예외 처리를 확장할 예정입니다.
 * </p>
 *
 * <ul>
 *   <li>WebClient 호출 실패 시 fallback 처리</li>
 *   <li>DAO 조회 실패 시 응답 캡슐화</li>
 *   <li>에러 발생 시 structured 로그에 에러 상태 포함</li>
 * </ul>
 *
 * @author 리희
 * @since 1.0
 */
@Service
public class MockBizServiceImpl implements MockBizService {

  /**
   * 구조화 로그 출력을 위한 {@link StructuredLogger} 인스턴스입니다.
   */
  private static final StructuredLogger logger
      = StructuredLoggerFactory.getLogger(MockBizServiceImpl.class);
  /**
   * mock 데이터 조회를 담당하는 DAO 포트입니다.
   *
   * <p>{@link com.rihee.alerting.mockservice.dao.MockSearchDatabase}는 내부 mock 데이터
   * (예: simple 조회 결과)를 반환하며, "Simple Call" 시나리오에서 사용됩니다.
   * </p>
   */
  private final MockSearchDatabase mockSearchDatabaseImpl;

  /**
   * 외부 mock 시스템 호출을 수행하는 포트입니다.
   *
   * <p>{@link com.rihee.alerting.mockservice.infra.MockExternalCall}은 외부 흐름 트리거 역할을 하며,
   * "Middle", "Branch", "Multi Layer" 시나리오에서 mock 호출 시나리오를 구성할 때 사용됩니다.
   * </p>
   */
  private final MockExternalCall mockExternalCallImpl;

  /**
   * {@code MockBizServiceImpl}의 생성자입니다.
   *
   * <p>mock 서비스의 비즈니스 흐름 구성에 필요한 DAO 및 외부 호출 컴포넌트를 주입받아 초기화합니다.
   * </p>
   *
   * @param mockSearchDatabaseImpl mock 데이터 조회를 위한 DAO 포트
   * @param mockExternalCallImpl 외부 mock 시스템 호출을 위한 인프라 포트
   */
  public MockBizServiceImpl(MockSearchDatabase mockSearchDatabaseImpl,
                            MockExternalCall mockExternalCallImpl) {
    this.mockSearchDatabaseImpl = mockSearchDatabaseImpl;
    this.mockExternalCallImpl = mockExternalCallImpl;
  }

  /**
   * "Simple Call" 시나리오에 대응하는 mock DAO 조회 흐름입니다.
   *
   * <p>{@link MockSearchDatabase#selectSimple()}을 호출하여 mock 결과를 가져오고,
   * 그 값을 structured 로그로 출력한 뒤 반환합니다.
   * </p>
   *
   * @return 조회된 mock 데이터 결과 문자열
   */
  @Override
  public String doSimpleSomething() {
    logger.info(LogType.BIZ, "Simple 서비스 레이어 시작.");
    String result = mockSearchDatabaseImpl.selectSimple();
    logger.info(LogType.BIZ, "Simple 서비스 레이어 결과 : {}", result);
    return result;
  }

  /**
   * "Middle Layer Call" 또는 "Branch Call"에서 중간 mock 호출을 수행하는 비즈니스 로직입니다.
   *
   * <p>{@link MockExternalCall#externalMiddleCall()}을 통해 외부 mock 호출을 실행하고,
   * 응답 결과를 structured 로그로 출력합니다.
   * </p>
   *
   * @return 외부 mock 응답 문자열
   */
  @Override
  public String doMiddleSomething() {
    logger.info(LogType.BIZ, "Middle 서비스 레이어 시작.");
    String result = mockExternalCallImpl.externalSimpleCall();
    logger.info(LogType.BIZ, "Middle 서비스 레이어 결과 : {}", result);
    return result;
  }

  /**
   * "Branch Call" 시나리오에서 복수의 외부 mock 호출을 병렬적으로 실행합니다.
   *
   * <p>{@link MockExternalCall#externalMiddleCall()} 및
   * {@link MockExternalCall#externalSimpleCall()}을 순차 호출하고,
   * 응답을 합쳐 structured 로그로 출력합니다.
   * </p>
   *
   * @return 결합된 mock 응답 문자열
   */
  @Override
  public String doBranchSomething() {
    logger.info(LogType.BIZ, "Branch 서비스 레이어 시작.");
    String result1 = mockExternalCallImpl.externalMiddleCall();
    String result2 = mockExternalCallImpl.externalSimpleCall();
    String result = result1 + " || " + result2;
    logger.info(LogType.BIZ, "Branch 서비스 레이어 결과 : {}", result);
    return result;
  }


  /**
   * "Multi Layer Call" 시나리오에서 중간 mock 호출을 트리거하는 로직입니다.
   *
   * <p>외부 mock 시스템으로부터 응답을 받아 structured 로그로 출력하며,
   * 이후 layer 흐름 검증 목적에 사용됩니다.
   * </p>
   *
   * @return 외부 mock 응답 문자열
   */
  @Override
  public String doMultiLayerSomething() {
    logger.info(LogType.BIZ, "MultiLayer 서비스 레이어 시작.");
    String result = mockExternalCallImpl.externalMiddleCall();
    logger.info(LogType.BIZ, "MultiLayer 서비스 레이어 결과 : {}", result);
    return result;
  }
}
