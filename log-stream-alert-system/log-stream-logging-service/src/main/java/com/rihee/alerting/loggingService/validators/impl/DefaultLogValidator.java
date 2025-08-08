package com.rihee.alerting.loggingService.validators.impl;

import com.rihee.alerting.common.constant.annotation.LogPolicy;
import com.rihee.alerting.common.constant.message.StructuredLogProperties;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.core.message.LogErrorMessage;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.validators.LogValidator;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLogValidator extends LogValidator {

  private static final Logger log = LoggerFactory.getLogger(DefaultLogValidator.class);

  private static final Predicate<Object> IS_VALID_STRING
      = value -> StringUtils.isNotBlank((String) value);
  private static final Map<String, Predicate<Object>> REQUIRED_FIELDS;

  static {
    Map<String, Predicate<Object>> predicateMap = new LinkedHashMap<>();
    for (StructuredLogProperties c : StructuredLogProperties.values()) {
      try {
        Field f = StructuredLogProperties.class.getField(c.name());      // enum 상수 필드
        // 필수적으로 검증해야한다는 것을 알리는 Annotation이 존재한다면 정책 추가
        LogPolicy policy = f.getAnnotation(LogPolicy.class);
        if (policy != null && policy.isEssential()) {
          predicateMap.put(c.getFieldName(), IS_VALID_STRING);
        }
      } catch (NoSuchFieldException e) {
        // values()에서 온 name은 반드시 필드가 존재해야 함. 실제론 발생 X.
        throw new IllegalStateException("Enum constant field not found: " + c.name(), e);
      }
    }
    REQUIRED_FIELDS = Collections.unmodifiableMap(predicateMap);
  }

  private DefaultLogValidator() {
  }

  public static LogValidator.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    LogProcessingContext resultMessages = new DefaultLogProcessingContext();
    for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
      LogMessage message = it.next();
      String reason = validateMessage(message);
      if (reason != null) {
        log.warn(reason);
        resultMessages.stackingLogMessage(LogErrorMessage.fromNormalMessage(message, reason));
        continue;
      }
      log.debug("Validate Success! : {}", message.getMessageKey());
      resultMessages.stackingLogMessage(message);
    }
    return ProcessResult.success(resultMessages);
  }

  public String validateMessage(LogMessage message) {
    for (Map.Entry<String, Predicate<Object>> entry : REQUIRED_FIELDS.entrySet()) {
      String key = entry.getKey();
      Predicate<Object> validate = entry.getValue();
      Object messageValue = message.get(key);
      if (!validate.test(messageValue)) {
        return String.format("Validate Failed! : [%s] field [value : %s]",
                                                  key, String.valueOf(messageValue));
      }
    }
    return null;
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
