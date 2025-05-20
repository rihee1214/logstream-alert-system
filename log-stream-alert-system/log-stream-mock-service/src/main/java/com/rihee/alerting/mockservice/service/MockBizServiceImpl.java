package com.rihee.alerting.mockservice.service;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import com.rihee.alerting.mockservice.dao.MockSearchDatabase;
import org.springframework.stereotype.Service;

/**
 * {@code MockBizServiceImpl}는 {@link MockBizService} 인터페이스의 구현체로,
 * 간단한 비즈니스 흐름을 구성하여 구조화된 로그 출력과 mock 데이터 조회를 수행합니다.
 *
 * <p>mock 환경에서 로깅 흐름 및 서비스 계층 호출 체계를 검증하기 위해 사용되며,
 * 내부적으로 {@link MockSearchDatabase}를 통해 고정된 결과를 조회합니다.
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
   * mock 데이터 조회를 담당하는 DAO 구현체입니다.
   */
  private final MockSearchDatabase mockSearchDatabaseImpl;

  /**
   * 생성자 주입을 통해 DAO 구현체를 주입받습니다.
   *
   * @param mockSearchDatabaseImpl mock 데이터 조회 DAO
   */
  public MockBizServiceImpl(MockSearchDatabase mockSearchDatabaseImpl) {
    this.mockSearchDatabaseImpl = mockSearchDatabaseImpl;
  }

  /**
   * mock DAO를 호출하여 단순 결과를 조회하고,
   * 그 결과를 구조화 로그로 출력한 후 반환합니다.
   *
   * @return 조회 결과 문자열 (ex. "simpleResult")
   */
  @Override
  public String doSomething() {
    String result = mockSearchDatabaseImpl.selectSimple();
    logger.info(LogType.BIZ, "서비스 레이어 결과 : {}", result);
    return result;
  }
}
