package com.rihee.alerting.loggingService.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface LogIngestService {

  void processingLog(JsonNode log);
}
