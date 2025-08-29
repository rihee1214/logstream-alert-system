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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CollectorType("kafka")
public final class KafkaLogCollectorAdapter extends LogCollectorPort implements CommitableLogProcessor {

  private static final Logger logger
      = LoggerFactory.getLogger(KafkaLogCollectorAdapter.class);

  private final Consumer<String, String> kafkaConsumer;
  private final Duration kafkaTimeoutMillis;

  private KafkaLogCollectorAdapter(KafkaConsumer<String, String> kafkaConsumer, int timeoutMillis) {
    this.kafkaConsumer = kafkaConsumer;
    this.kafkaTimeoutMillis = Duration.ofMillis(timeoutMillis);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      kafkaConsumer.wakeup();
      kafkaConsumer.close();
    }));
  }

  public static Builder builder() {
    return new Builder();
  }

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
        logger.debug(reason);
        newMessage = LogErrorMessage.fromOriginMessage(logMessage, messageKey, reason, stage());

        if (StringUtils.isBlank(messageKey)) {
          logger.warn("메시지 key가 없는 message입니다. : {}", logMessage);
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

  @Override
  public void commit() {
    try {
      kafkaConsumer.commitSync();
    } catch (CommitFailedException e) {
      logger.warn("Commit failed", e);
    }
  }

  private String generateKey(Map<String, Object> originLog) {
    String serviceName = String.valueOf(
                            originLog.get(StructuredLogProperties.SERVICE.getFieldName()));
    String hostName = String.valueOf(
                            originLog.get(StructuredLogProperties.HOST.getFieldName()));
    String containerName = String.valueOf(
                            originLog.get(StructuredLogProperties.CONTAINER.getFieldName()));

    return LogMessageKeyGenerator.generate(serviceName, hostName, containerName);
  }

  public static class Builder implements LogCollectorPort.Builder<KafkaLogCollectorAdapter> {

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

    @Override
    public KafkaLogCollectorAdapter build() {
      if (this.kafkaTopic == null || this.kafkaTopic.isBlank()) {
        throw new IllegalArgumentException("kafka Topic[kafka.topic]이 세팅되어있지 않습니다.");
      }

      KafkaConsumer<String, String> kafkaConsumer
          = new KafkaConsumer<>(new HashMap<>(consumerSetting));
      kafkaConsumer.subscribe(
          Arrays.stream(kafkaTopic.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
      );
      return new KafkaLogCollectorAdapter(kafkaConsumer, this.timeoutMillis);
    }
  }

}
