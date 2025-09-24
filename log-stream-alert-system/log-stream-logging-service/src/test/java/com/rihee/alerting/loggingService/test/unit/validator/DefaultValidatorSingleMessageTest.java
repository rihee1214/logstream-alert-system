package com.rihee.alerting.loggingService.test.unit.validator;

import static com.rihee.alerting.common.constant.logging.StructuredLogFields.CLASS;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.CONTAINER;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.HOST;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.LEVEL;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.LOG_TYPE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.MESSAGE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.NAME;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.PARENT_SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.SERVICE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.STACK_TRACE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.TIME_STAMP;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.TRACE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.rihee.alerting.loggingService.adapter.rule.validator.TestDefaultLogValidatorAdapter;
import com.rihee.alerting.loggingService.core.model.LogErrorMessage;
import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.model.LogNormalMessage;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.runtime.RuntimeBootstrapExtension;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(RuntimeBootstrapExtension.class)
public class DefaultValidatorSingleMessageTest {

  @Test
  @DisplayName("단일 정상 메시지를 받아서 해당 메시지를 검증한 후 다음 파이프라인으로 넘긴다.")
  void validates_single_message_and_emits_to_pipeline(TestDefaultLogValidatorAdapter adapter) {

    Map<String, Object> params = new HashMap<>(TEST_PARAM_MAP);

    LogMessage message = LogNormalMessage.fromOriginMessage(params, "abc");
    LogProcessingContext context = new DefaultLogProcessingContext();
    context.stackingLogMessage(message);

    // when: 검증기 처리
    ProcessResult result = adapter.process(context);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);

    LogMessage resultMessage = result.context().iterator().next();
    assertThat(resultMessage.isError()).isFalse();
    assertThat(resultMessage).isInstanceOf(LogNormalMessage.class);
  }

  @Test
  @DisplayName("단일 정상 메시지를 받아서 해당 메시지를 에러처리 후 다음 파이프라인으로 넘긴다.")
  void validates_single_message_and_emits_error_to_pipeline(TestDefaultLogValidatorAdapter adapter) {

    Map<String, Object> params = new HashMap<>(TEST_PARAM_MAP);
    params.remove(SERVICE.getFieldName());

    LogMessage message = LogNormalMessage.fromOriginMessage(params, "abc");
    LogProcessingContext context = new DefaultLogProcessingContext();
    context.stackingLogMessage(message);

    // when: 수집기 처리
    ProcessResult result = adapter.process(context);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);

    LogMessage resultMessage = result.context().iterator().next();
    assertThat(resultMessage.isError()).isTrue();
    assertThat(resultMessage).isInstanceOf(LogErrorMessage.class);
  }

  @Test
  @DisplayName("단일 에러 메시지를 받아서 해당 메시지를 검증한 후 다음 파이프라인으로 넘긴다.")
  void validates_single_error_message_and_emits_error_to_pipeline(TestDefaultLogValidatorAdapter adapter) {

    Map<String, Object> params = new HashMap<>(TEST_PARAM_MAP);
    params.remove(SERVICE.getFieldName());

    LogMessage origin = LogNormalMessage.fromOriginMessage(params, "abc");
    LogMessage message = LogErrorMessage.fromNormalMessage(origin, "테스트용 에러 발생", "collector");
    LogProcessingContext context = new DefaultLogProcessingContext();
    context.stackingLogMessage(message);

    // when: 수집기 처리
    ProcessResult result = adapter.process(context);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);

    LogMessage resultMessage = result.context().iterator().next();
    assertThat(resultMessage.isError()).isTrue();
    assertThat(resultMessage).isInstanceOf(LogErrorMessage.class);
  }

  private static final Map<String, Object> TEST_PARAM_MAP;

  static {
    Map<String,Object> PARAM_MAP = new HashMap<>();
    PARAM_MAP.put(LOG_TYPE.getFieldName(), "biz");
    PARAM_MAP.put(TIME_STAMP.getFieldName(), Instant.now().toString());
    PARAM_MAP.put(SERVICE.getFieldName(), "orders");
    PARAM_MAP.put(LEVEL.getFieldName(), "debug");
    PARAM_MAP.put(NAME.getFieldName(), "test");
    PARAM_MAP.put(CLASS.getFieldName(), "com.example.OrderService"); // 컬럼명은 "class"
    PARAM_MAP.put(MESSAGE.getFieldName(), "hello world");
    PARAM_MAP.put(HOST.getFieldName(), "ip-10-0-0-1");
    PARAM_MAP.put(CONTAINER.getFieldName(), "orders-0");
    PARAM_MAP.put(STACK_TRACE.getFieldName(), null);          // 없으면 null
    PARAM_MAP.put(TRACE_ID.getFieldName(), "abc123");
    PARAM_MAP.put(SPAN_ID.getFieldName(), "def456");
    PARAM_MAP.put(PARENT_SPAN_ID.getFieldName(), null);
    TEST_PARAM_MAP = Collections.unmodifiableMap(PARAM_MAP);
  }
}
