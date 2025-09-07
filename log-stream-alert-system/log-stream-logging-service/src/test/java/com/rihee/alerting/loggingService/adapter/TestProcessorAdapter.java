package com.rihee.alerting.loggingService.adapter;

public interface TestProcessorAdapter extends AutoCloseable {

  String id();

  void resetState();
}
