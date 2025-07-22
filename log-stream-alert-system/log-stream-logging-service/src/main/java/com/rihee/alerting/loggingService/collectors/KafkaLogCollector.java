package com.rihee.alerting.loggingService.collectors;

import com.rihee.alerting.loggingService.annotations.CollectorType;

@CollectorType("kafka")
public class KafkaLogCollector implements LogCollector {

  @Override
  public String getLogData() {

    return "";
  }
}
