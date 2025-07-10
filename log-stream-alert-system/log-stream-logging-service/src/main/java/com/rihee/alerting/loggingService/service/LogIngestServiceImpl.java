package com.rihee.alerting.loggingService.service;

import static com.rihee.alerting.common.constant.log.CallCommonProperties.TYPE;
import static com.rihee.alerting.common.constant.log.CallCommonProperties.ELAPSED_MS;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.METHOD;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.RESP_TRACE_ID;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.STATUS_CODE;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.STATUS_MESSAGE;
import static com.rihee.alerting.common.constant.log.HttpCallProperties.URI;

import com.rihee.alerting.loggingService.dao.LogIngestDao;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class LogIngestServiceImpl implements LogIngestService {

  private LogIngestDao scyllaDbLogIngestDao;

  public LogIngestServiceImpl(LogIngestDao scyllaDbLogIngestDao) {
    this.scyllaDbLogIngestDao = scyllaDbLogIngestDao;
  }

  @Override
  public void processingLog(Map<String, Object> log) {
    Map<String, Object> dataMap = new HashMap<>();
    for(String key : log.keySet()) {
      String modifiedKey = key.replace('.', '_');
      dataMap.put(modifiedKey, log.get(key));
    }

    scyllaDbLogIngestDao.insertLog(dataMap);
  }
}
