package com.rihee.alerting.loggingService.collectors.impl;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.collectors.LogCollector;

@CollectorType("kafka")
public class KafkaLogCollector implements LogCollector {

  @Override
  public String getLogData() {

    return "";
  }
}
