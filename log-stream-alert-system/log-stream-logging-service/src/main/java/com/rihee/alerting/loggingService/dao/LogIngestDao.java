package com.rihee.alerting.loggingService.dao;

import java.util.Map;

/**
 * {@code LogIngestDao}는 수신된 로그 데이터를 저장소에 기록하기 위한 DAO 계층의 표준 인터페이스입니다.
 *
 * <p>Kafka, 메시지 큐, 외부 API 등 다양한 수단을 통해 수신한 로그를
 * 내부 구조화된 형태로 저장소에 기록하는 데 사용되며, 구체적인 저장소 구현체(ScyllaDB, RDB, NoSQL 등)는
 * 이 인터페이스를 구현하여 독립적인 저장 로직을 제공합니다.
 *
 * <p>비즈니스 서비스나 수집기(Agent)는 이 인터페이스를 통해 저장소에 대한 의존성을 제거하고,
 * 향후 저장소의 교체 또는 확장 시에도 아키텍처 변경 없이 유연하게 대응할 수 있습니다.
 *
 * <p>⚠️ 본 인터페이스는 단순 키-값 기반의 구조화된 로그 저장만을 지원하며,
 * 트랜잭션 처리, 배치 처리, 다중 테이블 분산 저장 등 고급 기능은 구현체에 위임됩니다.
 *
 * @author 리희
 * @since 1.0
 */
public interface LogIngestDao {

  /**
   * 구조화된 로그 데이터를 저장소에 기록합니다.
   *
   * <p>입력된 {@code Map<String, Object>} 형태의 데이터는 로그 저장소 스키마에 맞게 사전 처리되어야 하며,
   * 필드명은 저장소의 컬럼명 또는 JSON 키와 일치해야 합니다.
   * 필드가 누락되었거나 타입이 일치하지 않을 경우, 구현체에 따라 예외가 발생하거나 무시될 수 있습니다.
   *
   * <p>본 메서드는 저장 실패에 대한 예외를 호출자에게 전달할 수 있으며,
   * 필요 시 {@code RuntimeException} 기반의 wrapping 처리를 통해
   * 외부 전파 및 알림 시스템 연계가 가능합니다.
   *
   * @param logData 저장할 로그 데이터. 필드명은 표준화된 구조(예: call_type, traceId 등)를 따라야 하며,
   *                값은 원시 타입 또는 문자열 형태로 변환된 상태여야 합니다.
   */
  void writeLog(Map<String, Object> logData);
}
