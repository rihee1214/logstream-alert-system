package com.rihee.alerting.loggingService.adapter.out.persistence;

import static com.rihee.alerting.common.constant.storage.NormalLogSchema.CLASS_NAME;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.CONTAINER;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.HOST;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.LOG_LEVEL;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.LOG_TYPE;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.LOG_VERSION_MAJOR;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.MESSAGE;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.META;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.PARENT_SPAN_ID;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.SERVICE;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.SPAN_ID;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.STACKTRACE;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.TIMESTAMP;
import static com.rihee.alerting.common.constant.storage.NormalLogSchema.TRACE_ID;

import com.rihee.alerting.common.constant.logging.StructuredLogFields;
import com.rihee.alerting.common.constant.storage.ErrorLogSchema;
import com.rihee.alerting.common.constant.storage.NormalLogSchema;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL 기반 영속 어댑터.
 *
 * <p>수집·검증 단계를 거친 {@link LogMessage}들을 PostgreSQL에 배치(PreparedBatch)로 저장한다.
 * 정상 로그와 에러 로그를 각각 별도의 테이블에 기록하며, 멱등성을 위해
 * {@code ON CONFLICT (message_id) DO NOTHING} 전략을 사용한다.
 *
 * <h2>역할</h2>
 * <ul>
 *   <li>파이프라인(out 단계)로부터 전달된 메시지들을 Jdbi 배치로 insert</li>
 *   <li>정상/에러 메시지를 구분하여 서로 다른 INSERT 쿼리 실행</li>
 *   <li>실행 결과를 요약 로그로 남겨 관측성 확보</li>
 * </ul>
 *
 * <h2>스키마/계약</h2>
 * <ul>
 *   <li>정상 로그: {@code logs_entries} 테이블</li>
 *   <li>에러 로그: {@code error_logs_entries} 테이블</li>
 *   <li>멱등키: {@code message_id} UNIQUE(또는 PK) 가정</li>
 *   <li>{@link NormalLogSchema}, {@link ErrorLogSchema}의 필드명을 DB 컬럼에 바인딩</li>
 * </ul>
 *
 * <h2>스레드·수명</h2>
 * <ul>
 *   <li>Jdbi/HikariCP를 내부적으로 보유하며, {@link #close()} 시 풀을 정리</li>
 *   <li>동일 어댑터 인스턴스는 복수 스레드에서 공유하지 않는 것을 권장(파이프라인 설계에 따름)</li>
 * </ul>
 *
 * @implNote SQL/테이블 네이밍은 추후 일관 규칙에 맞춰 정리될 수 있다.
 * @see LogPersistencePort
 * @see Jdbi
 * @see HikariConfig
 * @since 1.0
 */
@PersistenceType("postgres")
public final class PostgresPersistenceAdapter extends LogPersistencePort {

  /**
   * 정상 로그 INSERT 쿼리.
   */
  // TODO messageId에 대한 재고찰 필요.
  static final String NORMAL_INSERT_QUERY = """
      INSERT INTO logs_entries (
          logtype,
          timestamp,
          level,
          service,
          class_name,
          message,
          host,
          container,
          stacktrace,
          trace_id,
          span_id,
          parent_span_id,
          log_major_version,
          meta
      )
      VALUES (
          :logtype,
          :timestamp,
          :level,
          :service,
          :class_name,
          :message,
          :host,
          :container,
          :stacktrace,
          :trace_id,
          :span_id,
          :parent_span_id,
          :log_major_version,
          coalesce(:meta::jsonb, '{}'::jsonb)
      ) ON CONFLICT(message_id) DO NOTHING
      """;

  /**
   * 에러 로그 INSERT 쿼리.
   */
  static final String ERROR_INSERT_QUERY = """
      INSERT INTO error_logs_entries (
                      message_id, origin_log, reason, occurred_at, stage, log_version_major)
            VALUES (:message_id, :origin_log, :reason, :occurred_at, :stage, :log_version_major)
            ON CONFLICT(message_id) DO NOTHING
      """;

  private static final Logger log = LoggerFactory.getLogger(PostgresPersistenceAdapter.class);

  private final Jdbi jdbi;
  private final DataSource dataSource;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * 테스트 및 런타임에서 Hikari(DataSource)와 Jdbi를 주입받아 생성한다.
   *
   * @param jdbi       Jdbi 핸들
   * @param dataSource 커넥션 풀(DataSource)
   */
  PostgresPersistenceAdapter(Jdbi jdbi, DataSource dataSource) {
    this.jdbi = jdbi;
    this.dataSource = dataSource;
  }

  /**
   * 파이프라인 컨텍스트의 메시지들을 DB에 배치 저장한다.
   *
   * <p>동작:
   * <ol>
   *   <li>컨텍스트가 비어있지 않으면 Jdbi 핸들을 열고 정상/에러용 배치를 준비</li>
   *   <li>메시지별로 에러 여부를 판단해 각각의 배치에 바인딩·추가</li>
   *   <li>배치 실행 후 결과 요약 로그 출력(성공/중복 또는 noop/실패)</li>
   *   <li>원본 메시지를 그대로 다음 단계로 전달하기 위해 새 컨텍스트에 적재</li>
   * </ol>
   *
   * @param messages 이전 단계로부터 전달된 메시지 컨텍스트
   * @return 성공 결과와 함께, 동일 메시지를 담은 새 컨텍스트
   */
  @Override
  public ProcessResult process(LogProcessingContext messages) {
    LogProcessingContext result = new DefaultLogProcessingContext();
    if (!messages.isEmpty()) {
      jdbi.useHandle(handle -> {
        PreparedBatch normalBatch = handle.prepareBatch(NORMAL_INSERT_QUERY);
        PreparedBatch errorBatch = handle.prepareBatch(ERROR_INSERT_QUERY);

        int normalCount = 0;
        int errorCount = 0;

        for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
          LogMessage message = it.next();

          if (message.isError()) {
            for (ErrorLogSchema param : ErrorLogSchema.values()) {
              errorBatch.bind(param.getSchemaName(), message.get(param.getSchemaName()));
            }
            errorBatch.add();
            errorCount++;
          } else {
            // TODO 기능 완성 및 스키마 완성 필요
            normalBatch
                .bind(LOG_TYPE.getSchemaName(),
                        message.get(StructuredLogFields.LOG_TYPE.getFieldName()))
                .bind(TIMESTAMP.getSchemaName(),
                        message.get(StructuredLogFields.TIME_STAMP.getFieldName()))
                .bind(LOG_LEVEL.getSchemaName(),
                        message.get(StructuredLogFields.LEVEL.getFieldName()))
                .bind(SERVICE.getSchemaName(),
                        message.get(StructuredLogFields.SERVICE.getFieldName()))
                .bind(CLASS_NAME.getSchemaName(),
                        message.get(StructuredLogFields.CLASS.getFieldName()))
                .bind(MESSAGE.getSchemaName(),
                        message.get(StructuredLogFields.MESSAGE.getFieldName()))
                .bind(HOST.getSchemaName(),
                        message.get(StructuredLogFields.HOST.getFieldName()))
                .bind(CONTAINER.getSchemaName(),
                        message.get(StructuredLogFields.CONTAINER.getFieldName()))
                .bind(STACKTRACE.getSchemaName(),
                        message.get(StructuredLogFields.STACK_TRACE.getFieldName()))
                .bind(TRACE_ID.getSchemaName(),
                        message.get(StructuredLogFields.TRACE_ID.getFieldName()))
                .bind(SPAN_ID.getSchemaName(),
                        message.get(StructuredLogFields.SPAN_ID.getFieldName()))
                .bind(PARENT_SPAN_ID.getSchemaName(),
                        message.get(StructuredLogFields.PARENT_SPAN_ID.getFieldName()))
                .bind(LOG_VERSION_MAJOR.getSchemaName(),
                        message.get(LOG_VERSION_MAJOR.getSchemaName()))
                .bind(META.getSchemaName(),
                        message.get(META.getSchemaName()))
                .add();
            normalCount++;
          }

          result.stackingLogMessage(message);
        }

        if (normalCount > 0) {
          logBatchResult("normal", normalBatch.execute());
        }
        if (errorCount > 0) {
          logBatchResult("error", errorBatch.execute());
        }
      });
    }

    return ProcessResult.success(result);
  }

  /**
   * JDBC 배치 결과(int[])를 요약해서 로그로 남긴다.
   *
   * <p>규칙:
   * <ul>
   *   <li>{@code 1}: 성공한 행(INSERT/UPDATE 성공)</li>
   *   <li>{@code 0}: 영향 없음(여기서는 멱등 충돌 등으로 간주)</li>
   *   <li>{@link Statement#SUCCESS_NO_INFO}({@code -2}): 성공했으나 영향 행 수 미상 → 성공 처리</li>
   *   <li>{@link Statement#EXECUTE_FAILED}({@code -3}): 실패</li>
   * </ul>
   *
   * @param name   배치 이름(예: {@code normal}, {@code error})
   * @param result JDBC 배치 실행 결과 배열
   */
  private static void logBatchResult(String name, int[] result) {
    if (result == null) {
      log.warn("[batch:{}] result is null", name);
      return;
    }
    long success = Arrays.stream(result)
                          .filter(v -> v == 1 || v == Statement.SUCCESS_NO_INFO)  // 1 또는 -2
                          .count();
    long duplicateOrNoOp = Arrays.stream(result)
                                  .filter(v -> v == 0)                          // 0
                                  .count();
    long failed = Arrays.stream(result)
                        .filter(v -> v == Statement.EXECUTE_FAILED)             // -3
                        .count();
    int total = result.length;

    if (failed > 0) {
      log.warn("[batch:{}] total={}, success={}, duplicate/noop={}, failed={}, raw={}",
          name, total, success, duplicateOrNoOp, failed, Arrays.toString(result));
    } else {
      log.debug("[batch:{}] total={}, success={}, duplicate/noop={}",
          name, total, success, duplicateOrNoOp);
    }
  }

  /**
   * 빌더 엔트리 포인트.
   *
   * @return 어댑터 빌더
   */
  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

  /**
   * 내부 DataSource가 {@link AutoCloseable} 이면 안전하게 종료한다.
   * 이미 종료되었거나 종료가 불가능한 경우 조용히 무시한다.
   */
  @Override
  public void close() throws Exception {
    if (dataSource instanceof AutoCloseable ac && !closed.compareAndExchange(false, true)) {
      try {
        ac.close();
      } catch (Exception ignore) {
        // 오류가 발생하더라도 무시한다. 이미 종료된 자원이거나, 종료할 수 없는 자원임.
      }
    }
  }

  /**
   * HikariCP + Jdbi를 구성하는 빌더.
   *
   * <p>필수 키 누락 시 즉시 실패(Fail-fast). 비밀번호는 환경변수
   * {@code POSTGRES_CONNECT_PASSWORD} 에서 읽는다.
   */
  public static class Builder implements LogProcessorPort.Builder<PostgresPersistenceAdapter> {

    private HikariConfig config;

    /**
     * 설정 맵으로부터 HikariConfig를 생성한다.
     *
     * <p>필수:
     * <ul>
     *   <li>{@code postgres.connect.url}</li>
     *   <li>{@code postgres.connect.username}</li>
     *   <li>환경변수 {@code POSTGRES_CONNECT_PASSWORD}</li>
     * </ul>
     * 선택(풀):
     * <ul>
     *   <li>{@code postgres.setting.maximum.pool.size}</li>
     *   <li>{@code postgres.setting.minimum.pool.size}</li>
     *   <li>{@code postgres.setting.idle.timeout}</li>
     *   <li>{@code postgres.setting.connection.timeout}</li>
     *   <li>{@code postgres.setting.max.lifetime}</li>
     * </ul>
     *
     * @param setting 애플리케이션 설정
     * @return 빌더 자기 자신
     * @throws IllegalArgumentException 필수 값 누락/파싱 실패 시
     */
    @Override
    public LogProcessorPort.Builder<PostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {

      config = getHikariConfigFromSetting(setting);
      return this;
    }

    /**
     * 설정 맵을 파싱하여 {@link HikariConfig} 를 채운다.
     *
     * @param setting 설정 맵
     * @return 구성된 {@link HikariConfig}
     * @throws IllegalArgumentException 필수 항목 누락 또는 파싱 오류 시
     */
    private HikariConfig getHikariConfigFromSetting(Map<String, String> setting) {
      String url = setting.get("postgres.connect.url");
      String username = setting.get("postgres.connect.username");
      String password = System.getenv("POSTGRES_CONNECT_PASSWORD");
      if (!StringUtils.isNotBlank(url)) {
        throw new IllegalArgumentException("[설정 누락] postgres url 은 필수 항목입니다.");
      }
      if (!StringUtils.isNotBlank(username)) {
        throw new IllegalArgumentException("[설정 누락] postgres username 은 필수 항목입니다.");
      }
      if (!StringUtils.isNotBlank(password)) {
        throw new IllegalArgumentException("[설정 누락] postgres password 은 필수 항목입니다. "
                                  + "환경 변수 POSTGRES_CONNECT_PASSWORD 가 설정되어 있는지 확인하십시오.");
      }
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(url);
      config.setUsername(username);
      config.setPassword(password);

      // 커넥션 풀 설정 (필요에 따라 조정 가능)
      String tempMaximumPoolSize = setting.get("postgres.setting.maximum.pool.size");
      String tempMinimumPoolSize = setting.get("postgres.setting.minimum.pool.size");
      String tempIdleTimeout = setting.get("postgres.setting.idle.timeout");
      String tempConnectionTimeout = setting.get("postgres.setting.connection.timeout");
      String tempMaxLifeTime = setting.get("postgres.setting.max.lifetime");
      try {
        config.setMaximumPoolSize(Integer.parseInt(tempMaximumPoolSize));
        config.setMinimumIdle(Integer.parseInt(tempMinimumPoolSize));
        config.setIdleTimeout(Integer.parseInt(tempIdleTimeout));
        config.setConnectionTimeout(Integer.parseInt(tempConnectionTimeout));
        config.setMaxLifetime(Integer.parseInt(tempMaxLifeTime));
      } catch (RuntimeException e) {
        throw new IllegalArgumentException("커넥션 풀 세팅 중 문제가 발생하였습니다.", e);
      }

      // Postgres 에서 권장하는 드라이버 설정
      config.setDriverClassName("org.postgresql.Driver");
      return config;
    }

    /**
     * {@link HikariDataSource} 와 {@link Jdbi} 를 초기화하여 어댑터를 생성한다.
     *
     * @return {@link PostgresPersistenceAdapter} 인스턴스
     */
    @Override
    public PostgresPersistenceAdapter build() {
      DataSource dataSource = new HikariDataSource(config);
      Jdbi jdbi = Jdbi.create(dataSource);
      return new PostgresPersistenceAdapter(jdbi, dataSource);
    }
  }
}
