package com.rihee.alerting.loggingService.test.unit.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.adapter.Proc;
import com.rihee.alerting.loggingService.adapter.TestProcessorAdapter;
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
        @Proc(TestKafkaLogCollectorAdapter.class) TestKafkaLogCollectorAdapter adapter) {
    // given: 테스트용 Kafka 수집기 (MockConsumer 내부 주입)=


    Map<String, Object> params = new HashMap<>();
    params.put("logtype", "app");
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
    params.put("log_major_version", 1);
    // 테스트 레코드 1건 주입 (topic, offset, key, value)
    adapter.enqueue("log_topic", 0L, "k1", MapUtils.toJsonString(params));

    // 파이프라인으로 전달되는 메시지를 기록할 테스트 컨텍스트
    var ctx = new DefaultLogProcessingContext();

    // when: 수집기 처리
    ProcessResult result = adapter.process(ctx);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(ctx.size()).isEqualTo(1);
    assertThat(result.context().size()).isEqualTo(1);

    // (선택) 키/페이로드 등 추가 검증 — LogMessage API에 맞게 조정
    // assertThat(ctx.received.get(0).key()).isEqualTo("k1");

    // (선택) 커밋 경로 검증 — 예외 없이 실행되는지만 확인
    assertThatCode(adapter::commit).doesNotThrowAnyException();
  }
}
