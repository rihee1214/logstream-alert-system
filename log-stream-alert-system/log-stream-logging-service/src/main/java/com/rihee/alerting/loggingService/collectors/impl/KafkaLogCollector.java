package com.rihee.alerting.loggingService.collectors.impl;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.core.message.LogErrorMessage;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.message.LogNormalMessage;
import com.rihee.alerting.loggingService.core.pipeline.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.time.Duration;
import java.util.Arrays;
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

  private static final Logger logger
      = LoggerFactory.getLogger(KafkaLogCollector.class);

  private final Consumer<String, String> kafkaConsumer;
  private final Duration kafkaTimeoutMillis;

  private KafkaLogCollector(KafkaConsumer<String, String> kafkaConsumer, int timeoutMillis) {
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
        Map<String, Object> allLogs = MapUtils.fromJson(logMessage);
        newMessage = LogNormalMessage.fromOriginMessage(allLogs, messageKey);
      } catch (RuntimeException e) {
        logger.debug("로그 메시지 [key : {}]를 파싱할 수 없어 에러 로그로 처리합니다.", messageKey);
        newMessage = LogErrorMessage.fromOriginMessage(logMessage, messageKey);
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

    private Map<String, String> consumerSetting = new HashMap<>();
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
      KafkaConsumer<String, String> kafkaConsumer
          = new KafkaConsumer<>(new HashMap<>(consumerSetting));
      kafkaConsumer.subscribe(Arrays.asList(consumerSetting.get("kafka.topic").split(",")));
      return new KafkaLogCollector(kafkaConsumer, this.timeoutMillis);
    }
  }

}
