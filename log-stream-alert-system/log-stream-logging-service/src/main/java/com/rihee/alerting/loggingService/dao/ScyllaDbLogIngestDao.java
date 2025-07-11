package com.rihee.alerting.loggingService.dao;

import static com.rihee.alerting.loggingService.dao.cql.LogCqlTemplate.INSERT_LOG;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 * {@code ScyllaDbLogIngestDao}는 구조화된 로그 데이터를 ScyllaDB에 저장하는 DAO 구현체입니다.
 *
 * <p>Kafka 또는 외부 로그 수집기를 통해 수신된 로그 데이터를 ScyllaDB의 {@code service_log} 테이블에 INSERT하는 역할을 담당하며,
 * 로그 스키마 버전을 고정값으로 관리하여 버전 관리 기반의 진화 가능한 로그 저장 구조를 지원합니다.
 *
 * <p>실제 쿼리는 {@link com.rihee.alerting.loggingService.dao.cql.LogCqlTemplate#INSERT_LOG} 템플릿을 기반으로
 * 작성되며, {@code Map<String, Object>} 형태의 로그 데이터를 필드별로 바인딩하여 CQL Statement를 구성합니다.
 *
 * <p>ScyllaDB의 높은 처리량과 낮은 지연 특성을 활용하여 대량 로그 저장에 적합하도록 설계되어 있으며,
 * 성능 저하 없이 안정적인 운영이 가능하도록 설계되었습니다.
 *
 * @author 리희
 * @since 1.0
 */
@Repository
public class ScyllaDbLogIngestDao implements LogIngestDao {

  /** 현재 로그 스키마의 버전 번호. 추후 구조 변경을 대비하여 버전 관리 기반의 설계가 적용됨. */
  private static final int CURRENT_LOG_SCHEMA_VERSION = 1;
  private CqlSession session;

  /**
   * 수신한 로그 데이터를 ScyllaDB에 저장합니다.
   *
   * <p>입력 받은 로그 {@code Map} 객체의 각 필드를 CQL 쿼리의 명명된 파라미터에 매핑하여 저장하며,
   * 누락되거나 잘못된 필드는 내부적으로 null 처리되거나 CQL에서 무시됩니다.
   * 이 메서드는 실패 시 별도 예외 처리를 하지 않으며, Scylla 클러스터의 가용성을 전제로 작동합니다.
   *
   * <p>UUID는 로그 발생 시점 기준의 time-based UUID로 자동 생성되며,
   * 로그 스키마 버전은 현재 고정값 {@code 1}로 바인딩됩니다.
   *
   * <p>⚠️ 필드 이름이 {@code .} 포함 형태일 경우, 사전에 {@code _}로 치환된 상태여야 합니다.
   *
   * @param logData 저장 대상 로그 데이터. 키는 컬럼명, 값은 해당 로그의 값으로 구성됨.
   */
  @Override
  public void writeLog(Map<String, Object> logData) {
    SimpleStatement statement
        = SimpleStatement.builder(INSERT_LOG)
            .addNamedValue("uuid", Uuids.timeBased())
            .addNamedValue("log_schema_version", CURRENT_LOG_SCHEMA_VERSION)
            .addNamedValue("timestamp", logData.get("timestamp"))
            .addNamedValue("level", logData.get("level"))
            .addNamedValue("service", logData.get("service"))
            .addNamedValue("class", logData.get("class"))
            .addNamedValue("message", logData.get("message"))
            .addNamedValue("host", logData.get("host"))
            .addNamedValue("container", logData.get("container"))
            .addNamedValue("stacktrace", logData.get("stacktrace"))
            .addNamedValue("traceId", logData.get("traceId"))
            .addNamedValue("spanId", logData.get("spanId"))
            .addNamedValue("parentSpanId", logData.get("parentSpanId"))
            .addNamedValue("sampled", logData.get("sampled"))
            .addNamedValue("flags", logData.get("flags"))
            .addNamedValue("call_type", logData.get("call_type"))
            .addNamedValue("call_method", logData.get("call_method"))
            .addNamedValue("call_uri", logData.get("call_uri"))
            .addNamedValue("call_statusCode", logData.get("call_statusCode"))
            .addNamedValue("call_statusMessage", logData.get("call_statusMessage"))
            .addNamedValue("call_elapsedMs", logData.get("call_elapsedMs"))
            .addNamedValue("call_remoteTraceId", logData.get("call_remoteTraceId"))
            .build();

    session.execute(statement);
  }
}
