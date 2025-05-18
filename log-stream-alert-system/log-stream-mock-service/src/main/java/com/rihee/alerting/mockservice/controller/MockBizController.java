package com.rihee.alerting.mockservice.controller;

import com.rihee.alerting.common.annotation.StructuredGetMapping;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import com.rihee.alerting.mockservice.service.MockBizService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockBizController {

  private static final StructuredLogger logger
                = StructuredLoggerFactory.getLogger(MockBizController.class);
  private final MockBizService mockBizServiceImpl;

  public MockBizController(MockBizService mockBizServiceImpl) {
    this.mockBizServiceImpl = mockBizServiceImpl;
  }

  @StructuredGetMapping(value = "/simpleBiz", spanLabel = "simpleBiz")
  public String simpleBizMockupService() {
    logger.info(LogType.BIZ, "simpleBizMockup 서비스를 시작합니다.");
    mockBizServiceImpl.doSomething();
    return "simpleBizMockup Call End";
  }
}
