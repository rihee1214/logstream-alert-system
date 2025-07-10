package com.rihee.alerting.loggingService.dao;

import java.util.Map;

public interface LogIngestDao {

  void insertLog(Map<String, Object> logData);
}
