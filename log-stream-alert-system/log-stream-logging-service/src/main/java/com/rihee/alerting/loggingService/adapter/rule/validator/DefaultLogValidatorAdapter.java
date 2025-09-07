package com.rihee.alerting.loggingService.adapter.rule.validator;

import com.rihee.alerting.common.constant.annotation.LogPolicy;
import com.rihee.alerting.common.constant.message.StructuredLogProperties;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.core.model.LogErrorMessage;
import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code DefaultLogValidator}는 {@link StructuredLogProperties}에 선언된 필드 중
 * {@link LogPolicy#isEssential()}이 표시된 항목들을 필수로 간주하여,
 * {@link LogProcessingContext} 내 각 {@link LogMessage}의 유효성을 검증하는 기본 구현체입니다.
 *
 * <p>검증 실패 시 해당 메시지는 {@link LogErrorMessage}로 변환되어 결과 컨텍스트에 적재되고,
 * 검증을 통과한 메시지는 그대로 다음 단계로 전달됩니다.
 *
 * <p><strong>스레드 안전성:</strong> 본 구현은 불변(static) 검증 맵을 사용하며 상태를 저장하지 않으므로
 * 여러 스레드에서 안전하게 재사용할 수 있습니다.
 *
 * @see StructuredLogProperties
 * @see LogPolicy
 * @see LogProcessingContext
 * @see LogMessage
 * @see LogErrorMessage
 * @see LogValidatorPort
 */
@ValidatorType("default")
public final class DefaultLogValidatorAdapter extends LogValidatorPort implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(DefaultLogValidatorAdapter.class);

  // CharSequence 기반 필수 문자열 검증(널/공백 불가)
  private static final Predicate<Object> IS_VALID_STRING
      = value -> StringUtils.isNotBlank((String) value);
  // 기본(required by annotation) 필수 필드들
  private static final Map<String, Predicate<Object>> REQUIRED_FIELDS;

  static {
    Map<String, Predicate<Object>> predicateMap = new LinkedHashMap<>();
    for (StructuredLogProperties c : StructuredLogProperties.values()) {
      try {
        // enum 상수 필드에 직접 선언된 @LogPolicy 분석
        Field f = StructuredLogProperties.class.getField(c.name());
        LogPolicy policy = f.getAnnotation(LogPolicy.class);
        if (policy != null && policy.isEssential()) {
          predicateMap.put(c.getFieldName(), IS_VALID_STRING);
        }
      } catch (NoSuchFieldException e) {
        // values()의 name은 반드시 존재해야 함: 개발 시점 오류
        throw new IllegalStateException("Enum constant field not found: " + c.name(), e);
      }
    }
    REQUIRED_FIELDS = Collections.unmodifiableMap(predicateMap);
  }

  private DefaultLogValidatorAdapter() {
  }

  /**
   * {@link DefaultLogValidatorAdapter}를 생성하기 위한 빌더를 반환합니다.
   *
   * @return {@link DefaultLogValidatorAdapter} 전용 빌더
   */
  public static LogValidatorPort.Builder<?> builder() {
    return new Builder();
  }

  /**
   * 메시지 컨텍스트의 각 {@link LogMessage}를 검증합니다.
   *
   * <p>검증 실패한 메시지는 {@link LogErrorMessage}로 변환하여 결과 컨텍스트에 적재하고,
   * 검증에 성공한 메시지는 그대로 적재합니다. 검증 실패는 경고 로그 수준으로 기록되며,
   * 성공은 디버그 로그로 기록됩니다.
   *
   * @param messages 검증 대상이 담긴 입력 {@link LogProcessingContext}
   * @return 검증 결과(성공/실패 메시지가 적재된) {@link ProcessResult}
   */
  @Override
  public ProcessResult process(LogProcessingContext messages) {
    LogProcessingContext resultMessages = new DefaultLogProcessingContext();
    for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
      LogMessage message = it.next();
      String reason = validateMessage(message);
      if (reason != null) {
        // 치명도가 낮은(복구 가능) 데이터 검증 실패 → warn 수준 + 메시지 키 중심으로 로그 축약
        log.warn(reason);
        resultMessages.stackingLogMessage(
              LogErrorMessage.fromNormalMessage(message, reason, stage()));
        continue;
      }
      log.debug("Validate Success! : {}", message.getMessageKey());
      resultMessages.stackingLogMessage(message);
    }
    return ProcessResult.success(resultMessages);
  }

  /**
   * 단일 {@link LogMessage}에 대해 필수 필드의 존재 여부와 형식을 검사합니다.
   *
   * @param message 검증할 로그 메시지
   * @return 검증에 실패한 경우 실패 사유(사람이 읽을 수 있는 문자열),
   *         검증에 성공한 경우 {@code null}
   */
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

  @Override
  public void close() throws Exception {
    // 회수할 자원이 없으므로, 그대로 둔다.
  }

  /**
   * {@link DefaultLogValidatorAdapter} 인스턴스를 생성하기 위한 빌더입니다.
   *
   * <p>현재 구현에서는 외부 설정을 사용하지 않으며,
   * 어노테이션 {@link LogPolicy#isEssential()} 기반 필수 필드만 검증합니다.
   *
   * @see #withProperties(Map)
   * @see #build()
   */
  public static class Builder implements LogValidatorPort.Builder<DefaultLogValidatorAdapter> {

    /**
     * 빌더에 설정 값을 전달합니다.
     *
     * <p><strong>주의:</strong> 현재 구현에서는 전달된 {@code setting}을 사용하지 않습니다.
     * 추후 확장을 위해 시그니처만 유지됩니다.
     *
     * @param setting 설정 값 맵(현재 미사용)
     * @return 이 빌더 자신(메서드 체이닝용)
     */
    @Override
    public LogValidatorPort.Builder<DefaultLogValidatorAdapter>
                                                withProperties(Map<String, String> setting) {
      return this;
    }

    /**
     * {@link DefaultLogValidatorAdapter} 인스턴스를 생성합니다.
     *
     * @return 새 {@link DefaultLogValidatorAdapter} 인스턴스
     */
    @Override
    public DefaultLogValidatorAdapter build() {
      return new DefaultLogValidatorAdapter();
    }
  }
}
