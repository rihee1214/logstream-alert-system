package com.rihee.alerting.mockservice.service;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import com.rihee.alerting.mockservice.dao.MockSearchDatabase;
import org.springframework.stereotype.Service;

@Service
public class MockBizServiceImpl implements MockBizService {

  private static final StructuredLogger logger
      = StructuredLoggerFactory.getLogger(MockBizServiceImpl.class);
  private final MockSearchDatabase mockSearchDatabaseImpl;

  public MockBizServiceImpl(MockSearchDatabase mockSearchDatabaseImpl) {
    this.mockSearchDatabaseImpl = mockSearchDatabaseImpl;
  }

  @Override
  public String doSomething() {
    String result = mockSearchDatabaseImpl.selectSimple();
    logger.info(LogType.BIZ, "서비스 레이어 결과 : {}", result);
    return result;
  }
}
