package com.rihee.alerting.loggingService.test.unit.collector;

import static com.rihee.alerting.loggingService.adapter.in.collector.TestKafkaLogCollectorAdapter.DEFAULT_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.adapter.in.collector.TestKafkaLogCollectorAdapter;
import com.rihee.alerting.loggingService.core.model.LogErrorMessage;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.runtime.RuntimeBootstrapExtension;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * KafkaCollectorSingleMessageTest 클래스는 단일 Kafka 메시지를 수집하고 처리하는 로직을 테스트하는 단위 테스트를 포함한다.
 *
 * <p>이 테스트 클래스는 JUnit 5의 확장 기능을 활용하며 런타임 파이프라인 초기화를 수행한다.</p>
 *
 * <p>이 테스트 클래스는 {@link RuntimeBootstrapExtension}을 통해 테스트 어댑터의 설정 및 리소스 정리를 자동으로 처리하며
 * 테스트 케이스마다 동일한 스레드 내에서 동작해야 하는 제약을 가진다.</p>
 */
@ExtendWith(RuntimeBootstrapExtension.class)
public class KafkaCollectorSingleMessageTest {

  @Test
  @DisplayName("단일 Kafka 메시지를 수집하고 이를 성공적으로 파이프라인으로 전달하는 경우를 검증한다.")
  void collects_single_message_and_emits_to_pipeline(TestKafkaLogCollectorAdapter adapter) {

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
    adapter.enqueue(DEFAULT_TOPIC, 0L, "k1", MapUtils.toJsonString(params));

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
  @DisplayName("단일 Kafka 메시지를 수집했으나 key 생성이 불가한 경우를 시뮬레이션하여 메시지가 스킵되는 동작을 검증한다.")
  void collects_single_message_and_skip_create_message_key_error(
                                                  TestKafkaLogCollectorAdapter adapter) {

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
    adapter.enqueue(DEFAULT_TOPIC, 0L, null, MapUtils.toJsonString(params));

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
  @DisplayName("단일 Kafka 메시지를 수집한 후 파싱이 불가능한 경우 에러 메시지로 변환하는 로직을 검증한다.")
  void collects_single_message_and_emit_error_message(TestKafkaLogCollectorAdapter adapter) {

    // 테스트 레코드 1건 주입 (topic, offset, key, value)
    adapter.enqueue(DEFAULT_TOPIC, 0L, "k123", "에러 발생용 메시지입니다.");

    // when: 수집기 처리
    ProcessResult result = adapter.process(new DefaultLogProcessingContext());

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);
    assertThat(result.context().iterator().next()).isInstanceOf(LogErrorMessage.class);
    assertThat(result.context().iterator().next().isError()).isTrue();

    // (선택) 커밋 경로 검증 — 예외 없이 실행되는지만 확인
    assertThatCode(adapter::commit).doesNotThrowAnyException();
  }
}
