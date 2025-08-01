package com.rihee.alerting.loggingService.collectors.impl;

import com.jsoniter.JsonIterator;
import com.jsoniter.spi.TypeLiteral;
import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.core.LogMessage;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CollectorType("kafka")
public final class KafkaLogCollector extends LogCollector {

  private static Logger logger = LoggerFactory.getLogger(KafkaLogCollector.class);

  private Consumer<String, String> kafkaConsumer;

  private KafkaLogCollector(Map<String, Object> setting) {
    this.kafkaConsumer = new KafkaConsumer<>(setting);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public List<LogMessage> process(List<LogMessage> messages) {
    // TODO Setting 에서 읽어서 몇초마다 한번씩 메시지를 가져올지 세팅하도록 해야함
    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));

    // TODO 잘못된 메시지가 있으면 (값이 없다거나 등등) 없애버리는 역할 필요.
    //  전반적으로 오류가 나더라도 생존의 여지를 높여야함.
    for (ConsumerRecord<String, String> record : records) {
      Map<String, Object> logMessage
            = JsonIterator.deserialize(record.value(), new TypeLiteral<>(){});
      messages.add(new LogMessage(logMessage));
    }

    return messages;
  }

  public void commitSync() {
    try {
      kafkaConsumer.commitSync();
    } catch (CommitFailedException e) {
      logger.warn("Commit failed", e);
    }
  }

  protected static class Builder implements LogCollector.Builder<KafkaLogCollector> {

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

    @Override
    public Builder withProperties(Map<String, String> setting) {
      KEY_MAPPING.forEach((key, value) -> {
        String settingValue = setting.get(key);
        if (settingValue == null || settingValue.isBlank()) {
          throw new IllegalArgumentException("필수 Kafka 설정이 빠졌습니다: [key :" + key + "]");
        }
        consumerSetting.put(value, settingValue);
      });
      return this;
    }

    @Override
    public KafkaLogCollector build() {
      return new KafkaLogCollector(consumerSetting);
    }
  }

}
