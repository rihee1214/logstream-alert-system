package com.rihee.alerting.loggingService.test.unit.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.core.model.LogErrorMessage;
import com.rihee.alerting.loggingService.testinfra.common.TestParameter;
import com.rihee.alerting.loggingService.adapter.in.collector.TestKafkaLogCollectorAdapter;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.runtime.RuntimeBootstrapExtension;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(RuntimeBootstrapExtension.class)
public class KafkaCollectorSingleMessageTest {

  @Test
  @DisplayName("단일 메시지를 수집하여 파이프라인으로 전달한다")
  void collects_single_message_and_emits_to_pipeline(
        @TestParameter(TestKafkaLogCollectorAdapter.class) TestKafkaLogCollectorAdapter adapter) {

    Map<String, Object> params = new HashMap<>();
    params.put("logtype", "biz");
    params.put("timestamp", Instant.now().toString());
    params.put("service", "orders");
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
    // 테스트 레코드 1건 주입 (topic, offset, key, value)
    adapter.enqueue("log_topic", 0L, "k1", MapUtils.toJsonString(params));

    // when: 수집기 처리
    ProcessResult result = adapter.process(new DefaultLogProcessingContext());

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);

    // (선택) 커밋 경로 검증 — 예외 없이 실행되는지만 확인
    assertThatCode(adapter::commit).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("단일 메시지를 수집하여 key 생성 불가 메시지를 스킵한다.")
  void collects_single_message_and_skip_create_message_key_error(
      @TestParameter(TestKafkaLogCollectorAdapter.class) TestKafkaLogCollectorAdapter adapter) {

    Map<String, Object> params = new HashMap<>();
    params.put("logtype", "biz");
    params.put("timestamp", Instant.now().toString());
    params.put("service", null);  // 발생할 수 없고, 발생 해서는 안되는 케이스
    params.put("class", "com.example.OrderService"); // 컬럼명은 "class"
    params.put("message", "hello world");
    params.put("host", null);
    params.put("container", null);
    params.put("stacktrace", null);          // 없으면 null
    params.put("traceId", "abc123");
    params.put("spanId", "def456");
    params.put("parentSpanId", null);        // 없으면 null
    // 테스트 레코드 1건 주입 (topic, offset, key, value)
    adapter.enqueue("log_topic", 0L, null, MapUtils.toJsonString(params));

    // when: 수집기 처리
    ProcessResult result = adapter.process(new DefaultLogProcessingContext());

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isFalse();
    assertThat(result.shouldCommit()).isFalse();
    assertThat(result.context().size()).isEqualTo(0);

    // (선택) 커밋 경로 검증 — 예외 없이 실행되는지만 확인
    assertThatCode(adapter::commit).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("단일 메시지를 수집한 후 파싱 불가능 판정으로, 에러 메시지로 변환한다.")
  void collects_single_message_and_emit_error_message(
      @TestParameter(TestKafkaLogCollectorAdapter.class) TestKafkaLogCollectorAdapter adapter) {

    // 테스트 레코드 1건 주입 (topic, offset, key, value)
    adapter.enqueue("log_topic", 0L, "k123", "에러 발생용 메시지입니다.");

    // when: 수집기 처리
    ProcessResult result = adapter.process(new DefaultLogProcessingContext());

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);
    assertThat(result.context().iterator().next()).isInstanceOf(LogErrorMessage.class);

    // (선택) 커밋 경로 검증 — 예외 없이 실행되는지만 확인
    assertThatCode(adapter::commit).doesNotThrowAnyException();
  }
}
