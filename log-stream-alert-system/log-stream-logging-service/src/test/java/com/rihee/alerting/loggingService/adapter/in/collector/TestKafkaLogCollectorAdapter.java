package com.rihee.alerting.loggingService.adapter.in.collector;

import com.rihee.alerting.common.constant.message.StructuredLogProperties;
import com.rihee.alerting.common.identity.LogMessageKeyGenerator;
import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.core.model.LogErrorMessage;
import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.model.LogNormalMessage;
import com.rihee.alerting.loggingService.core.pipeline.api.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

/**
 * TODO 패키지 다르게 만들고 테스트용 mockup으로 만들기.
 */
@CollectorType("kafka")
public final class TestKafkaLogCollectorAdapter extends LogCollectorPort
                                            implements CommitableLogProcessor {

  private final Consumer<String, String> kafkaConsumer;
  private final Duration kafkaTimeoutMillis;

  private TestKafkaLogCollectorAdapter(Consumer<String, String> kafkaConsumer, int timeoutMillis) {
    this.kafkaConsumer = kafkaConsumer;
    this.kafkaTimeoutMillis = Duration.ofMillis(timeoutMillis);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      kafkaConsumer.wakeup();
      kafkaConsumer.close();
    }));
  }

  /**
   * {@link TestKafkaLogCollectorAdapter} 생성을 위한 {@link Builder}를 반환합니다.
   *
   * <p>일반적으로 사용 예시는 다음과 같습니다:
   * <pre>{@code
   * KafkaLogCollectorAdapter collector =
   *     KafkaLogCollectorAdapter.builder()
   *         .withProperties(kafkaSettings)
   *         .build();
   * }</pre>
   *
   * @return 새로운 {@link Builder} 인스턴스
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Kafka에서 메시지를 polling하여 {@link LogProcessingContext}에 적재합니다.
   *
   * <p>처리 과정:
   * <ol>
   *   <li>Kafka로부터 {@link ConsumerRecords}를 수신</li>
   *   <li>각 {@link ConsumerRecord}를 파싱 후 {@link LogMessage}로 변환</li>
   *   <li>변환 중 예외 발생 시 {@link LogErrorMessage}로 wrapping</li>
   *   <li>생성된 메시지를 {@code contextMessage}에 추가</li>
   * </ol>
   *
   * @param contextMessage 로그 메시지들을 담을 파이프라인 context
   * @return 처리 결과를 포함한 {@link ProcessResult}
   */
  @Override
  public ProcessResult process(LogProcessingContext contextMessage) {
    ConsumerRecords<String, String> records = kafkaConsumer.poll(this.kafkaTimeoutMillis);

    for (ConsumerRecord<String, String> record : records) {
      String messageKey = record.key();
      String logMessage = record.value();
      LogMessage newMessage = null;
      try {
        Map<String, Object> allLogComponents = MapUtils.fromJson(logMessage);
        if (StringUtils.isBlank(messageKey)) {
          messageKey = generateKey(allLogComponents);
        }
        newMessage = LogNormalMessage.fromOriginMessage(allLogComponents, messageKey);
      } catch (RuntimeException e) {
        String reason = String.format("로그 메시지 [key : %s]를 파싱할 수 없어 에러 로그로 처리합니다.", messageKey);
//        logger.debug(reason);
        newMessage = LogErrorMessage.fromOriginMessage(logMessage, messageKey, reason, stage());

        if (StringUtils.isBlank(messageKey)) {
//          logger.warn("메시지 key가 없는 message입니다. : {}", logMessage);
          continue;
        }
      }

      contextMessage.stackingLogMessage(newMessage);
    }

    if (contextMessage.isEmpty()) {
      return ProcessResult.error(contextMessage,
          String.format("[%s] Collect 과정에서 수신받은 데이터가 없습니다.", this.getClass().getSimpleName()));
    }

    return ProcessResult.success(contextMessage);
  }

  /**
   * Kafka consumer의 오프셋을 동기적으로 커밋합니다.
   *
   * <p>이 메서드는 수집 및 처리가 완료된 레코드들을
   * "정상적으로 처리되었다"라고 Kafka 브로커에 알리기 위해 호출됩니다.
   * <br>즉, 커밋이 성공적으로 수행되면 이후 동일한 레코드가 다시
   * 소비되지 않도록 보장합니다.
   *
   * <p>{@link CommitFailedException}이 발생할 경우,
   * 커밋 실패 로그만 남기며 예외는 전파하지 않습니다.
   * 이는 수집 파이프라인의 안정성을 위한 선택입니다.
   */
  @Override
  public void commit() {
    try {
      kafkaConsumer.commitSync();
    } catch (CommitFailedException e) {
//      logger.warn("Commit failed", e);
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

  /**
   * {@code KafkaLogCollectorAdapter} 생성을 위한 Builder 클래스입니다.
   *
   * <p>책임:
   * <ul>
   *   <li>Kafka Consumer 설정을 외부 properties로부터 로딩</li>
   *   <li>필수 Kafka 설정 검증 (bootstrap.servers, group.id 등)</li>
   *   <li>Kafka Topic 구독 등록</li>
   *   <li>{@link TestKafkaLogCollectorAdapter} 인스턴스 생성</li>
   * </ul>
   *
   * <p>이 클래스의 {@link #build()} 메서드는 여러 번 호출할 수 있으며,
   * 호출 시마다 새로운 {@link TestKafkaLogCollectorAdapter} 인스턴스를 생성합니다.
   */
  public static class Builder implements LogCollectorPort.Builder<TestKafkaLogCollectorAdapter> {

    private static final Map<String, String> KEY_MAPPING = Map.of(
        "kafka.bootstrap.servers",  ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        "kafka.group.id",           ConsumerConfig.GROUP_ID_CONFIG,
        "kafka.enable.auto.commit", ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        "kafka.auto.offset.reset",  ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        "kafka.max.poll.records",   ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
        "kafka.key.deserializer",   ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        "kafka.value.deserializer", ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
    );

    private Map<String, String> consumerSetting = new HashMap<>();
    private int timeoutMillis = 1000;
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
      KEY_MAPPING.forEach((key, value) -> {
        String settingValue = setting.get(key);
        if (settingValue == null || settingValue.isBlank()) {
          throw new IllegalArgumentException("필수 Kafka 설정이 빠졌습니다: [key :" + key + "]");
        }
        consumerSetting.put(value, settingValue);
      });

      this.kafkaTopic = setting.get("kafka.topic");
      this.timeoutMillis = Integer.parseInt(setting.getOrDefault("kafka.max.poll.timeout", "1000"));
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
      if (this.kafkaTopic == null || this.kafkaTopic.isBlank()) {
        throw new IllegalArgumentException("kafka Topic[kafka.topic]이 세팅되어있지 않습니다.");
      }

      Consumer<String, String> kafkaConsumer
          = new MockConsumer<>(OffsetResetStrategy.LATEST);
//          = new KafkaConsumer<>(new HashMap<>(consumerSetting));
      kafkaConsumer.subscribe(
          Arrays.stream(kafkaTopic.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
      );
      return new TestKafkaLogCollectorAdapter(kafkaConsumer, this.timeoutMillis);
    }
  }

}
