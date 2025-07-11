package com.rihee.alerting.loggingService.dao.cql;

/**
 * {@code LogCqlTemplate} 클래스는 ScyllaDB에 로그 데이터를 저장하기 위한
 * CQL 쿼리 템플릿을 정의한 유틸리티 클래스입니다.
 *
 * <p>이 클래스는 로그 저장을 위한 CQL 구문을 중앙에서 관리함으로써
 * 쿼리 재사용성과 일관성을 확보하고, 유지보수를 용이하게 합니다.
 *
 * <p>인스턴스 생성을 막기 위해 생성자는 private으로 선언되어 있으며,
 * 상속이 불가능한 final 클래스로 정의되어 있습니다.
 *
 * <p>사용 예:
 * <pre>{@code
 *   session.execute(SimpleStatement.newInstance(LogCqlTemplate.INSERT_LOG, params));
 * }</pre>
 *
 * <p>바인딩 시 모든 파라미터는 {@code :uuid}, {@code :timestamp}와 같은 이름 기반 파라미터로
 * 전달되어야 하며, 명확한 파라미터 이름을 통해 순서에 의존하지 않는 안전한 쿼리 실행이 가능합니다.
 *
 * @author 리희
 * @since 1.0
 */
public final class LogCqlTemplate {

  private LogCqlTemplate() {}

  /**
   * {@code log_keyspace.service_log} 테이블에 구조화된 로그 데이터를 저장하기 위한 INSERT CQL 문입니다.
   *
   * <p>해당 쿼리는 명명된 파라미터(named parameter)를 사용하여 유연한 바인딩을 가능하게 하며,
   * 로그 필드의 명확한 대응을 통해 가독성과 유지보수를 향상시킵니다.
   *
   * <p>주요 특징:
   * <ul>
   *   <li>{@code uuid}는 datastax 라이브러리의 {@code Uuids.timeBased()}로 생성하여 주입합니다.</li>
   *   <li>{@code log_schema_version}은 현재 버전 {@code 1}로 고정되어 사용됩니다.</li>
   *   <li>그 외 필드들은 수신한 로그 메시지를 기반으로 가공 또는 필터링하여 바인딩됩니다.</li>
   * </ul>
   *
   * <p>컬럼명은 내부 정책에 따라 {@code dot(.) → underscore(_)}로 변경해야하며,
   * 명명 일관성을 유지하기 위한 사전 처리 로직이 필요합니다.
   */
  public static final String INSERT_LOG = """
        INSERT INTO log_keyspace.service_log (
            uuid,
            log_schema_version,
            timestamp,
            level,
            service,
            class,
            message,
            host,
            container,
            stacktrace,
            traceId,
            spanId,
            parentSpanId,
            sampled,
            flags,
            call_type,
            call_method,
            call_uri,
            call_statusCode,
            call_statusMessage,
            call_elapsedMs,
            call_remoteTraceId
        ) VALUES (
            :uuid,
            :log_schema_version,
            :timestamp,
            :level,
            :service,
            :class,
            :message,
            :host,
            :container,
            :stacktrace,
            :traceId,
            :spanId,
            :parentSpanId,
            :sampled,
            :flags,
            :call_type,
            :call_method,
            :call_uri,
            :call_statusCode,
            :call_statusMessage,
            :call_elapsedMs,
            :call_remoteTraceId
        );
        """;
}
