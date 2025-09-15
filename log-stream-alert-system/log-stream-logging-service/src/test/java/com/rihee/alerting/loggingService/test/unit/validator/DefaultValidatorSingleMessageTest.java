package com.rihee.alerting.loggingService.test.unit.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.rihee.alerting.loggingService.adapter.rule.validator.TestDefaultLogValidatorAdapter;
import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.model.LogNormalMessage;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.runtime.RuntimeBootstrapExtension;
import com.rihee.alerting.loggingService.testinfra.common.Proc;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(RuntimeBootstrapExtension.class)
public class DefaultValidatorSingleMessageTest {

  @Test
  @DisplayName("단일 정상 메시지를 받아서 해당 메시지를 검증한 후 다음 파이프 라인으로 넘긴다.")
  void collects_single_message_and_emits_to_pipeline(
      @Proc(TestDefaultLogValidatorAdapter.class) TestDefaultLogValidatorAdapter adapter) {

    Map<String, Object> params = new HashMap<>();
    params.put("logtype", "biz");
    params.put("timestamp", Instant.now().toString());
    params.put("service", "orders");
    params.put("level", "debug");
    params.put("name", "test");
    params.put("class", "com.example.OrderService"); // 컬럼명은 "class"
    params.put("message", "hello world");
    params.put("host", "ip-10-0-0-1");
    params.put("container", "orders-0");
    params.put("stacktrace", null);          // 없으면 null
    params.put("traceId", "abc123");
    params.put("spanId", "def456");
    params.put("parentSpanId", null);        // 없으면 null
    params.put("sampled", Boolean.TRUE);
    params.put("flags", 0);

    LogMessage message = LogNormalMessage.fromOriginMessage(params, "");
    LogProcessingContext context = new DefaultLogProcessingContext();
    context.stackingLogMessage(message);
    // 테스트 레코드 1건 주입 (topic, offset, key, value)


    // when: 수집기 처리
    ProcessResult result = adapter.process(context);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);
  }
}
