package com.rihee.alerting.loggingService.service;

import com.rihee.alerting.loggingService.dao.LogIngestDao;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * {@code LogIngestServiceImpl}는 {@link LogIngestService} 인터페이스의 구현체로서,
 * 수신된 로그 데이터를 구조화한 후 ScyllaDB에 저장하는 역할을 담당합니다.
 *
 * <p>입력된 로그 데이터의 키를 표준화된 형식으로 변환(예: {@code call.uri → call_uri})한 뒤,
 * DAO 계층을 통해 실제 저장소로 전달합니다.
 *
 * <p>이 클래스는 주로 Kafka 리스너 또는 REST API 등의 수신 지점에서 호출되며,
 * 로그 저장 정책이나 사전 가공 로직을 포함하는 서비스 계층의 구현체입니다.
 *
 * @author 리희
 * @since 1.0
 */
@Service
public class LogIngestServiceImpl implements LogIngestService {

  private LogIngestDao scyllaDbLogIngestDao;

  /**
   * {@code LogIngestServiceImpl}의 생성자.
   *
   * @param scyllaDbLogIngestDao 로그 데이터를 저장하는 DAO 구현체
   */
  public LogIngestServiceImpl(LogIngestDao scyllaDbLogIngestDao) {
    this.scyllaDbLogIngestDao = scyllaDbLogIngestDao;
  }

  /**
   * 로그 데이터를 수신 받아 키를 표준화하고, DAO를 통해 저장합니다.
   *
   * <p>입력된 {@code Map<String, Object>} 형태의 로그 데이터는
   * 모든 키에서 마침표('.')를 밑줄('_')로 치환하여,
   * DB 컬럼 이름과 충돌이 없도록 전처리됩니다.
   *
   * <p>그 후, {@link LogIngestDao#writeLog(Map)}를 호출하여
   * 영구 저장소에 실제 저장을 시도합니다.
   *
   * @param log 원본 로그 데이터 맵
   */
  @Override
  public void processingLog(Map<String, Object> log) {
    Map<String, Object> dataMap = new HashMap<>();
    for(String key : log.keySet()) {
      String modifiedKey = key.replace('.', '_');
      dataMap.put(modifiedKey, log.get(key));
    }

    scyllaDbLogIngestDao.writeLog(dataMap);
  }
}
