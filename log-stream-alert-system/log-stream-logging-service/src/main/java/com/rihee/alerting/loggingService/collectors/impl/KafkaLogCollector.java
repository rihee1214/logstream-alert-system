package com.rihee.alerting.loggingService.collectors.impl;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.core.message.LogNormalMessage;
import com.rihee.alerting.loggingService.core.pipeline.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CollectorType("kafka")
public final class KafkaLogCollector extends LogCollector implements CommitableLogProcessor {

  private static Logger logger = LoggerFactory.getLogger(KafkaLogCollector.class);

  private final Consumer<String, String> kafkaConsumer;
  private final Duration kafkaTimeoutMillis;

  private KafkaLogCollector(Map<String, Object> setting, int timeoutMillis) {
    this.kafkaConsumer = new KafkaConsumer<>(setting);
    this.kafkaTimeoutMillis = Duration.ofMillis(timeoutMillis);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public LogProcessingContext process(LogProcessingContext messages) {
    ConsumerRecords<String, String> records = kafkaConsumer.poll(this.kafkaTimeoutMillis);

    for (ConsumerRecord<String, String> record : records) {
      try {
        Map<String, Object> logMessage = MapUtils.fromJson(record.value());
        messages.stackingLogMessage(new LogNormalMessage(logMessage));
      } catch (RuntimeException e) {
        // TODO JSON 파싱 도중 문제가 발생한 경우, 여기에서 LogErrorMessage를 만들어서 넣도록 해야한다.
      }

    }

    return messages;
  }

  @Override
  public void commit() {
    try {
      kafkaConsumer.commitSync();
    } catch (CommitFailedException e) {
      logger.warn("Commit failed", e);
    }
  }

  public static class Builder implements LogCollector.Builder<KafkaLogCollector> {

    private static final Map<String, String> KEY_MAPPING = Map.of(
        "kafka.bootstrap.servers", "kafka.bootstrap.servers",
        "kafka.topic", "kafka.topic",
        "kafka.group.id", "kafka.group.id",
        "kafka.enable.auto.commit", "enable.auto.commit",
        "kafka.auto.offset.reset", "auto.offset.reset",
        "kafka.max.poll.records", "max.poll.records",
        "kafka.key.deserializer", "key.deserializer",
        "kafka.value.deserializer", "value.deserializer"
    );

    private Map<String, Object> consumerSetting = new HashMap<>();
    private int timeoutMillis = 1000;

    @Override
    public Builder withProperties(Map<String, String> setting) {
      KEY_MAPPING.forEach((key, value) -> {
        String settingValue = setting.get(key);
        if (settingValue == null || settingValue.isBlank()) {
          throw new IllegalArgumentException("필수 Kafka 설정이 빠졌습니다: [key :" + key + "]");
        }
        consumerSetting.put(value, settingValue);
      });
      this.timeoutMillis = Integer.parseInt(setting.getOrDefault("kafka.max.poll.timeout", "1000"));
      return this;
    }

    @Override
    public KafkaLogCollector build() {
      return new KafkaLogCollector(consumerSetting, this.timeoutMillis);
    }
  }

}
