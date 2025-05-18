package com.rihee.alerting.mockservice.dao;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import org.springframework.stereotype.Component;

@Component
public class MockSearchDatabaseImpl implements MockSearchDatabase {

  private static final StructuredLogger logger
      = StructuredLoggerFactory.getLogger(MockSearchDatabase.class);

  @Override
  public String selectSimple() {
    String result = "simpleResult";
    logger.info(LogType.BIZ, "select 결과 : {}", result);
    return result;
  }
}
