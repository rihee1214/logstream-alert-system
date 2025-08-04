package com.rihee.alerting.loggingService.validators.impl;

import com.rihee.alerting.loggingService.core.message.LogErrorMessage;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.validators.LogValidator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultLogValidator extends LogValidator {

  private DefaultLogValidator() {

  }

  public static LogValidator.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public LogProcessingContext process(LogProcessingContext messages) {
    LogProcessingContext resultMessages = new DefaultLogProcessingContext();
    for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
      // TODO 검증하는 로직 넣어야함
      //  검증에 실패하면 error message를 넣어서 persistence 영역에서 처리하도록 처리한다.
      //  검증에 성공하면 그 메시지 그대로 처리할 수 있도록 넘긴다.
      LogMessage message = it.next();
      if (message.get("") instanceof String) {
        resultMessages.stackingLogMessage(new LogErrorMessage(new HashMap<>()));
        continue;
      }
      resultMessages.stackingLogMessage(message);
    }

    return resultMessages;
  }

  public static class Builder implements LogValidator.Builder<DefaultLogValidator> {

    @Override
    public LogValidator.Builder<DefaultLogValidator> withProperties(Map<String, String> setting) {
      return this;
    }

    @Override
    public DefaultLogValidator build() {
      return new DefaultLogValidator();
    }
  }
}
