package com.rihee.alerting.loggingService.test.unit.testinfra.collector;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;

public class SharedTestKafkaConsumer {

  private static final AtomicReference<MockConsumer<String, String>> REF
                                                                = new AtomicReference<>();
  private static final OffsetResetStrategy STRATEGY = OffsetResetStrategy.EARLIEST;

  private static MockConsumer<String, String> ensure() {
    MockConsumer<String, String> mc = REF.get();
    if (mc == null) {
      mc = new MockConsumer<>(STRATEGY);
      REF.compareAndSet(null, mc);
    }
    return REF.get();
  }

  /**
   * 현재 공유 MockConsumer 반환 (없으면 생성).
   */
  public static MockConsumer<String, String> get() {
    return ensure();
  }

  /**
   * 고유 토픽(파티션 0) 추가 + 구독/리밸런스/오프셋 초기화.
   */
  public static TopicPartition addTopic(String topic) {
    TopicPartition tp = new TopicPartition(topic, 0);

    try (MockConsumer<String, String> c = ensure();) {
      c.subscribe(List.of(topic));
      // 테스트용 리밸런스 시뮬레이션
      c.rebalance(List.of(tp));
      // 시작 오프셋 0으로 초기화
      c.updateBeginningOffsets(Map.of(tp, 0L));
    }

    return tp;
  }

  /**
   * 레코드 주입 헬퍼 (오프셋 자동 증가).
   */
  public static void addRecords(TopicPartition tp, String... values) {
    try (MockConsumer<String, String> c = ensure();) {
      long[] next = { 0L };
      Arrays.stream(values).forEach(v ->
          c.addRecord(new ConsumerRecord<>(tp.topic(), tp.partition(), next[0]++, null, v)));
    }
  }

  /**
   * 커스텀 레코드 주입 (키/값/오프셋 지정).
   */
  public static void addRecord(ConsumerRecord<String, String> record) {
    try (MockConsumer<String, String> c = ensure();) {
      c.addRecord(record);
    }
  }

  /**
   * poll 한 번 실행 (테스트 중 SUT가 직접 poll한다면 호출 불필요).
   */
  public static void pollOnce(Duration timeout) {
    try (MockConsumer<String, String> c = ensure();) {
      c.poll(timeout);
    }
  }

  /**
   * 각 테스트 종료 후 깨끗하게 리셋.
   */
  public static void reset() {
    MockConsumer<String, String> c = REF.get();
    if (c != null) {
      try {
        // MockConsumer는 close 후 재생성이 안전함
        c.unsubscribe();
        c.close();
      } catch (Exception ignored) {
        // 실패해도 상관 없음.
      }
      REF.set(null);
    }
  }

  private SharedTestKafkaConsumer() {}
}
