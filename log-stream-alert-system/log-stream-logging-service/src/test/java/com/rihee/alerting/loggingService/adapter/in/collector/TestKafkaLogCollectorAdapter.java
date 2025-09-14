package com.rihee.alerting.loggingService.adapter.in.collector;

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

/**
 * 테스트 전용 Kafka 로그 수집기 어댑터.
 *
 * <p>내부적으로 {@link MockConsumer}를 사용해 입력을 시뮬레이션하고,
 * 실제 운영용 {@link KafkaLogCollectorAdapter}에 위임하여 파이프라인을 검증합니다.
 * 테스트 편의를 위해 {@link #enqueue(String, long, String, String)}(레코드 주입) 등의 도우미를 제공합니다.
 *
 * <p><b>스레드 모델(매우 중요):</b>
 * 이 클래스는 <em>스레드-국소(ThreadLocal)</em> 하네스를 사용합니다.
 * 따라서 <b>하나의 인스턴스를 여러 스레드가 공유하면 안 됩니다.</b>
 * 테스트를 멀티 스레드로 실행할 수는 있지만,
 * <b>같은 인스턴스를 다른 스레드에서 재사용하지 마세요.</b><br>
 * 각 스레드는 반드시 {@link #createNewInstance()}를 통해 자신만의 하네스를 초기화해야 하며,
 * 테스트 종료 시 {@link #close()}가 호출되어 ThreadLocal이 정리되어야 합니다.
 *
 * <p>본 어댑터는 <b>프로덕션 코드에서 사용하지 마십시오.</b> 테스트 전용입니다.
 */
@CollectorType("kafka")
public final class TestKafkaLogCollectorAdapter extends LogCollectorPort
                                  implements CommitableLogProcessor, TestProcessorAdapter {

  /**
   * 스레드별 테스트 하네스 보관소.
   *
   * <p>테스트 메서드 시작 시 {@link #createNewInstance()}로 바인딩되고,
   * 종료 시 {@link #close()}에서 제거됩니다.
   */
  private static final ThreadLocal<KafkaLogCollectorResource> HARNESS_THREAD_LOCAL
                                                                    = new ThreadLocal<>();
  /**
   * 빌드 시 결정되는 파티션 목록(기본적으로 각 토픽의 파티션 0).
   */
  private final List<TopicPartition> partitions;

  private TestKafkaLogCollectorAdapter(List<TopicPartition> partitions) {
    this.partitions = partitions;
  }

  /**
   * 동적으로 호출되는 Builder 팩토리 메서드.
   *
   * <p>리플렉션/플러그인 로딩 경로에서 <b>반드시</b> 존재해야 합니다.
   */
  public static Builder builder() {
    return new Builder();
  }

  private KafkaLogCollectorResource requireHarness() {
    KafkaLogCollectorResource r = HARNESS_THREAD_LOCAL.get();
    if (r == null) {
      throw new IllegalStateException("createNewInstance() 먼저 호출하세요.");
    }
    return r;
  }

  /**
   * 테스트 레코드를 현재 스레드의 {@link MockConsumer}에 주입합니다.
   *
   * <p>사전에 같은 스레드에서 {@link #createNewInstance()}가 호출되어 있어야 합니다.
   * 기본 구현은 파티션 {@code 0}에 기록합니다.
   *
   * @param topic  토픽명
   * @param offset 레코드 오프셋
   * @param key    레코드 키(없으면 {@code null})
   * @param value  레코드 값(JSON 등)
   * @throws IllegalStateException 하네스 미초기화(현재 스레드에 바인딩 없음)인 경우
   */
  public void enqueue(String topic, long offset, String key, String value) {
    // 단일 파티션(0) 기준; 필요시 확장
    requireHarness()
        .consumer().addRecord(new ConsumerRecord<>(topic, 0, offset, key, value));
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
    return requireHarness().adapter().process(contextMessage);
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
    KafkaLogCollectorResource resource = requireHarness();
    // 원본이 커밋을 노출하면 위임, 아니면 mock의 commitSync 사용
    if (resource.adapter() instanceof CommitableLogProcessor cp) {
      cp.commit();
    } else {
      resource.consumer().commitSync();
    }
  }

  /**
   * 원본 로그 맵에서 메시지 키를 생성합니다(테스트 편의 메서드).
   *
   * <p>실제 로직은 내부 어댑터에 위임합니다.
   */
  private String generateKey(Map<String, Object> originLog) {
    return requireHarness().adapter().generateKey(originLog);
  }

  /**
   * 현재 스레드용 테스트 하네스를 초기화하고 바인딩합니다.
   *
   * <p>동작:
   * <ul>
   *   <li>{@link OffsetResetStrategy#EARLIEST}로
   *      {@link org.apache.kafka.clients.consumer.MockConsumer} 생성</li>
   *   <li>사전 계산된 {@code partitions}를 assign하고 시작 오프셋을 모두 {@code 0}으로 설정</li>
   *   <li>{@link KafkaLogCollectorAdapter}로 감싼 후 내부 {@code ThreadLocal}에 저장</li>
   * </ul>
   *
   * <p><b>스레드 모델:</b> 테스트는 멀티 스레드로 실행할 수 있으나,
   * <b>같은 인스턴스를 여러 스레드가 공유해서는 안 됩니다.</b>
   * 각 스레드는 반드시 이 메서드를 호출해 자신의 하네스를 초기화해야 합니다.
   *
   * <p>초기화 도중 예외가 발생하면 생성된 {@code MockConsumer}를 닫고
   * {@link IllegalStateException}을 던집니다.</p>
   *
   * @throws IllegalStateException 하네스 초기화에 실패한 경우
   */
  @Override
  public void createNewInstance() {
    if (HARNESS_THREAD_LOCAL.get() != null) {
      this.close();
    }

    MockConsumer<String, String> mockConsumer = null;
    KafkaLogCollectorResource resource = null;
    try {
      mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
      mockConsumer.assign(partitions);
      mockConsumer.updateBeginningOffsets(partitions.stream()
          .collect(Collectors.toMap(tp -> tp, tp -> 0L)));
      for (TopicPartition tp : partitions) {
        mockConsumer.seek(tp, 0L);
      }

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

  /**
   * 현재 스레드에서 바인딩된 하네스를 해제합니다.
   *
   * <p>일반적으로 테스트 종료 시 호출되어 {@link ThreadLocal} 누수를 방지합니다.
   * (실제 자원은 모의 객체이므로 별도 해제가 필요하지 않습니다)
   */
  @Override
  public void close() {
    KafkaLogCollectorResource r = HARNESS_THREAD_LOCAL.get();
    try {
      if (r != null) {
        r.adapter().close(); // adapter.close() 호출
      }
    } catch (Exception ignore) {
      // 테스트 하네스라 조용히 무시
    } finally {
      HARNESS_THREAD_LOCAL.remove();
    }
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

      return new TestKafkaLogCollectorAdapter(tps);
    }
  }

  /**
   * 스레드-국소 하네스 리소스 묶음.
   *
   * <p>현재 스레드에서만 접근해야 합니다.
   */
  private record KafkaLogCollectorResource(KafkaLogCollectorAdapter adapter,
                                           MockConsumer<String, String> consumer,
                                           List<TopicPartition> topics) {
  }
}
