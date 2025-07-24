package com.rihee.alerting.loggingService.collectors.impl;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.collectors.LogCollector.Builder;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.Consumer;

@CollectorType("kafka")
public final class KafkaLogCollector extends LogCollector {

  private Consumer<String, String> kafkaConsumer;

  private KafkaLogCollector(){

  }

  @Override
  public List<String> getLogDatas() {

    return null;
  }

  public void commit(){
    try {
      kafkaConsumer.commitSync();
    } catch (CommitFailedException e) {
//      log.warn("Commit failed", e);
    }
  }

  public static class Builder implements LogCollector.Builder<KafkaLogCollector> {

    @Override
    public Builder withProperties(Properties setting) {
      return null;
    }

    @Override
    public KafkaLogCollector build() {
      return new KafkaLogCollector();
    }
  }

}
