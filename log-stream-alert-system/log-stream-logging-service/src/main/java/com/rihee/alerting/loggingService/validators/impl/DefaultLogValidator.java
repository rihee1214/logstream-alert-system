package com.rihee.alerting.loggingService.validators.impl;

import com.rihee.alerting.common.constant.annotation.LogPolicy;
import com.rihee.alerting.common.constant.log.StructuredLogProperties;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.core.message.LogErrorMessage;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.validators.LogValidator;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
    Map<String, Predicate<Object>> predicateMap = new HashMap<>();
    for (Field logField : StructuredLogProperties.class.getFields()) {
      LogPolicy policy = logField.getAnnotation(LogPolicy.class);
      if (policy != null && policy.isEssential()) {
        try {
          // enum 상수 인스턴스를 리플렉션으로 얻어옵니다.
          // StructuredLogProperties는 public enum 이므로 접근 제약이 없지만,
          // get(null)은 반드시 IllegalAccessException을 선언하게 되어 있어 예외를 처리해야 합니다.
          StructuredLogProperties enumConstant = (StructuredLogProperties) logField.get(null);
          predicateMap.put(enumConstant.getFieldName(), IS_VALID_STRING);
        } catch (IllegalAccessException e) {
          // 이 예외는 발생 가능성이 매우 낮지만, Java 문법상 반드시 처리해야 하므로 RuntimeException으로 감쌉니다.
          throw new RuntimeException("Failed to access enum constant: " + logField.getName(), e);
        }
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
  public LogProcessingContext process(LogProcessingContext messages) {
    LogProcessingContext resultMessages = new DefaultLogProcessingContext();
    for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
      LogMessage message = it.next();
      if (isValidationFailed(message)) {
        resultMessages.stackingLogMessage(LogErrorMessage.fromNormalMessage(message));
        continue;
      }
      log.debug("Validate Success! : {}", message.getMessageKey());
      resultMessages.stackingLogMessage(message);
    }
    return resultMessages;
  }

  public boolean isValidationFailed(LogMessage message) {
    for (Map.Entry<String, Predicate<Object>> entry : REQUIRED_FIELDS.entrySet()) {
      String key = entry.getKey();
      Predicate<Object> validate = entry.getValue();
      Object messageValue = message.get(key);
      if (!validate.test(messageValue)) {
        log.info("Validate Failed! : {} in {} field [value : {}]",
                          message.getMessageKey(), key, messageValue);
        return true;
      }
    }
    return false;
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
