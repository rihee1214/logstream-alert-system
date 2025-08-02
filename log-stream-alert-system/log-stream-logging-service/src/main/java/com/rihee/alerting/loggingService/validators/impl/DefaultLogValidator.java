package com.rihee.alerting.loggingService.validators.impl;

import com.rihee.alerting.loggingService.core.message.LogErrorMessage;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.validators.LogValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DefaultLogValidator extends LogValidator {

  @Override
  public List<LogMessage> process(List<LogMessage> messages) {
    List<LogMessage> resultMessages = new ArrayList<>();
    for (LogMessage message : messages) {
      // TODO 검증하는 로직 넣어야함
      //  검증에 실패하면 error message를 넣어서 persistence 영역에서 처리하도록 처리한다.
      //  검증에 성공하면 그 메시지 그대로 처리할 수 있도록 넘긴다.
      if (message.get("") instanceof String) {
        resultMessages.add(new LogErrorMessage(new HashMap<>()));
        continue;
      }
      resultMessages.add(message);
    }

    return resultMessages;
  }
}
