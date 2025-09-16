package com.rihee.alerting.loggingService.adapter.in.collector;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.core.pipeline.api.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;


@CollectorType("kafka")
public final class TestKafkaLogCollectorAdapter extends LogCollectorPort
                                                implements CommitableLogProcessor {

  private final KafkaLogCollectorAdapter adapter;
  private final MockConsumer<String, String> consumer;

  private TestKafkaLogCollectorAdapter(KafkaLogCollectorAdapter adapter,
                                        MockConsumer<String, String> consumer) {
    this.adapter = adapter;
    this.consumer = consumer;
  }

  public void enqueue(String topic, long offset, String key, String value) {
    // 단일 파티션(0) 기준; 필요시 확장
    consumer.addRecord(new ConsumerRecord<>(topic, 0, offset, key, value));
  }

  /**
   * 현재 스레드에 바인딩된 하네스를 통해 수집/전달을 수행합니다.
   *
   * @param contextMessage 파이프라인 컨텍스트
   * @return 처리 결과
   * @throws IllegalStateException 하네스 미초기화(현재 스레드에 바인딩 없음)인 경우
   */
  @Override
  public ProcessResult process(LogProcessingContext contextMessage) {
    return adapter.process(contextMessage);
  }

  /**
   * 커밋 경로를 수행합니다.
   *
   * <p>내부 어댑터가 {@link CommitableLogProcessor}를 구현하면 해당 구현에 위임하고,
   * 그렇지 않으면 {@link MockConsumer#commitSync()}를 호출합니다.
   *
   * @throws IllegalStateException 하네스 미초기화(현재 스레드에 바인딩 없음)인 경우
   */
  @Override
  public void commit() {
    adapter.commit();
  }

  /**
   * 현재 스레드에서 바인딩된 하네스를 해제합니다.
   *
   * <p>일반적으로 테스트 종료 시 호출되어 {@link ThreadLocal} 누수를 방지합니다.
   * (실제 자원은 모의 객체이므로 별도 해제가 필요하지 않습니다)
   */
  @Override
  public void close() {
    try {
      adapter.close();
    } catch (Exception ignore) {
      // 정리에 실패하더라도 무시하고 종료. 테스트용 이기에 실패하더라도 문제 없음.
    }
  }

  /**
   * 동적으로 호출되는 Builder 팩토리 메서드.
   *
   * <p>리플렉션/플러그인 로딩 경로에서 <b>반드시</b> 존재해야 합니다.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * 현재 스레드에서 바인딩된 하네스를 해제합니다.
   *
   * <p>일반적으로 테스트 종료 시 호출되어 {@link ThreadLocal} 누수를 방지합니다.
   * (실제 자원은 모의 객체이므로 별도 해제가 필요하지 않습니다)
   */
  public static class Builder implements LogCollectorPort.Builder<TestKafkaLogCollectorAdapter> {

    private KafkaLogCollectorAdapter.Builder originBuilder;
    private String kafkaTopic;

    /**
     * Kafka 설정을 수용합니다.
     *
     * @param setting 설정 맵
     * @return this
     * @throws IllegalArgumentException 필수 설정 누락 시
     */
    @Override
    public Builder withProperties(Map<String, String> setting) {
      this.originBuilder = KafkaLogCollectorAdapter.builder().withProperties(setting);
      this.kafkaTopic = this.originBuilder.getKafkaTopic();
      return this;
    }

    /**
     * 설정된 토픽을 기반으로 테스트 어댑터를 생성합니다.
     *
     * <p>각 토픽에 대해 파티션 0을 사용합니다.
     *
     * @return 테스트용 {@link TestKafkaLogCollectorAdapter}
     * @throws IllegalArgumentException 토픽 미설정 등 비정상 입력
     */
    @Override
    public TestKafkaLogCollectorAdapter build() {
      List<String> topics = Arrays.stream(kafkaTopic.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .toList();
      List<TopicPartition> tps = topics.stream()
          .map(t -> new TopicPartition(t, 0))
          .toList();

      MockConsumer<String, String> mockConsumer = null;
      try {
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        mockConsumer.assign(tps);
        mockConsumer.updateBeginningOffsets(tps.stream()
            .collect(Collectors.toMap(tp -> tp, tp -> 0L)));
        for (TopicPartition tp : tps) {
          mockConsumer.seek(tp, 0L);
        }

        KafkaLogCollectorAdapter adapter = new KafkaLogCollectorAdapter(mockConsumer);
        return new TestKafkaLogCollectorAdapter(adapter, mockConsumer);
      } catch (Exception e) {
        if (mockConsumer != null) {
          mockConsumer.close();
        }
        throw new IllegalStateException(e);
      }
    }
  }
}
