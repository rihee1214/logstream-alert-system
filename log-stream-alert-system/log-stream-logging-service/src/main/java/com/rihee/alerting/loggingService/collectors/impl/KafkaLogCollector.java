package com.rihee.alerting.loggingService.collectors.impl;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.core.LogMessage;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.Consumer;

@CollectorType("kafka")
public final class KafkaLogCollector extends LogCollector {

  private Consumer<String, String> kafkaConsumer;

  private KafkaLogCollector() {

  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public List<LogMessage> process(List<LogMessage> messages) {

    return null;
  }

  public void commit() {
    try {
      kafkaConsumer.commitSync();
    } catch (CommitFailedException e) {
//      log.warn("Commit failed", e);
    }
  }

  protected static class Builder implements LogCollector.Builder<KafkaLogCollector> {

    private String uri;

    @Override
    public Builder withProperties(Map<String, String> setting) {
      this.uri = setting.get("");
      return this;
    }

    @Override
    public KafkaLogCollector build() {
      return new KafkaLogCollector();
    }
  }

}
