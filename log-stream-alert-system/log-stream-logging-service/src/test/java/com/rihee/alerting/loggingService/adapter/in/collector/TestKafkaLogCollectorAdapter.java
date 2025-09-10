package com.rihee.alerting.loggingService.adapter.in.collector;

import com.rihee.alerting.common.constant.message.StructuredLogProperties;
import com.rihee.alerting.common.identity.LogMessageKeyGenerator;
import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.core.pipeline.api.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.testinfra.common.TestProcessorAdapter;
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
                                  implements CommitableLogProcessor, TestProcessorAdapter {

  private static final ThreadLocal<KafkaLogCollectorResource> HARNESS_THREAD_LOCAL
                                                                    = new ThreadLocal<>();
  private final List<TopicPartition> partitions;

  private TestKafkaLogCollectorAdapter(List<TopicPartition> partitions) {
    this.partitions = partitions;
  }

  /**
   * 동적으로 호출되는 Builder 메서드 입니다.<br>
   * 무조건 존재해야 하는 method.
   */
  public static Builder builder() {
    return new Builder();
  }

  // ---- 테스트 편의: 레코드 주입/오프셋 초기화 ----
  public void enqueue(String topic, long offset, String key, String value) {
    // 단일 파티션(0) 기준; 필요시 확장
    HARNESS_THREAD_LOCAL.get()
        .consumer().addRecord(new ConsumerRecord<>(topic, 0, offset, key, value));
  }

  /**
   * 필요하면 시작 오프셋을 재지정할 때 사용.
   */
  public void resetBeginningOffsets(long offset) {
    HARNESS_THREAD_LOCAL.get()
        .consumer().updateBeginningOffsets(
          partitions.stream().collect(Collectors.toMap(tp -> tp, tp -> offset))
        );
  }

  @Override
  public ProcessResult process(LogProcessingContext contextMessage) {
    return HARNESS_THREAD_LOCAL.get().adapter().process(contextMessage);
  }

  @Override
  public void commit() {
    // 원본이 커밋을 노출하면 위임, 아니면 mock의 commitSync 사용
    if (HARNESS_THREAD_LOCAL.get().adapter() instanceof CommitableLogProcessor cp) {
      cp.commit();
    } else {
      HARNESS_THREAD_LOCAL.get().consumer().commitSync();
    }
  }

  /**
   * 원본 로그로부터 service, host, container 정보를 추출하여
   * 고유한 메시지 key를 생성합니다.
   *
   * <p>Kafka 레코드에 key가 존재하지 않는 경우를 대비해 사용되며,
   * 정상적인 상황에서는 호출될 가능성이 거의 없습니다.
   * 다만 message key가 누락된 경우에도 로그 메시지를 추적할 수 있도록
   * {@link LogMessageKeyGenerator}를 통해 대체 key를 생성합니다.
   *
   * @param originLog 수집된 로그의 원본 key-value 맵
   * @return 생성된 고유한 로그 메시지 key
   */
  private String generateKey(Map<String, Object> originLog) {
    String serviceName = String.valueOf(
                            originLog.get(StructuredLogProperties.SERVICE.getFieldName()));
    String hostName = String.valueOf(
                            originLog.get(StructuredLogProperties.HOST.getFieldName()));
    String containerName = String.valueOf(
                            originLog.get(StructuredLogProperties.CONTAINER.getFieldName()));

    return LogMessageKeyGenerator.generate(serviceName, hostName, containerName);
  }

  @Override
  public void createNewInstance() {
    MockConsumer<String, String> mockConsumer = null;
    KafkaLogCollectorResource resource = null;
    try {
      mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
      mockConsumer.assign(partitions);
      mockConsumer.updateBeginningOffsets(partitions.stream()
          .collect(Collectors.toMap(tp -> tp, tp -> 0L)));

      resource = new KafkaLogCollectorResource(new KafkaLogCollectorAdapter(mockConsumer),
                                                                          mockConsumer, partitions);
    } catch (Exception e) {
      if (mockConsumer != null) {
        mockConsumer.close();
      }
      throw new IllegalStateException(e);
    }

    HARNESS_THREAD_LOCAL.set(resource);
  }

  @Override
  public void close() throws Exception {
    HARNESS_THREAD_LOCAL.remove();
  }

  public static class Builder implements LogCollectorPort.Builder<TestKafkaLogCollectorAdapter> {

    private KafkaLogCollectorAdapter.Builder originBuilder;
    private String kafkaTopic;

    /**
     * 주어진 properties를 바탕으로 Kafka consumer 설정을 적용합니다.
     *
     * @param setting Kafka 관련 설정값 map
     * @return 현재 builder 인스턴스
     * @throws IllegalArgumentException 필수 설정값이 누락된 경우
     */
    @Override
    public Builder withProperties(Map<String, String> setting) {
      this.originBuilder = KafkaLogCollectorAdapter.builder().withProperties(setting);
      this.kafkaTopic = this.originBuilder.getKafkaTopic();
      return this;
    }

    /**
     * 설정된 값들을 기반으로 Kafka Consumer를 생성하고,
     * 지정된 topic을 구독하는 {@link TestKafkaLogCollectorAdapter}를 빌드합니다.
     *
     * <p>이 메서드는 여러 번 호출할 수 있으며, 호출될 때마다 새로운
     * {@link TestKafkaLogCollectorAdapter} 인스턴스를 반환합니다.
     *
     * @return 초기화된 KafkaLogCollectorAdapter 인스턴스
     * @throws IllegalArgumentException topic이 설정되지 않은 경우
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

      return new TestKafkaLogCollectorAdapter(tps);
    }
  }

  private record KafkaLogCollectorResource(KafkaLogCollectorAdapter adapter,
                                                  MockConsumer<String, String> consumer,
                                                  List<TopicPartition> topics) {

  }
}
