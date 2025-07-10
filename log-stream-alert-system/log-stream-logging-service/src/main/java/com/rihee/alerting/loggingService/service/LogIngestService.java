package com.rihee.alerting.loggingService.service;

import java.util.Map;

public interface LogIngestService {

  void processingLog(Map<String, Object> log);
}
