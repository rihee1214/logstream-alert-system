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

@PersistenceType("postgres")
public final class PostgresPersistenceAdapter extends LogPersistencePort {

  // TODO 테이블 명을 더 명확하게 바꾸고, class 컬럼 같이 모호한 요소 변경 및, messageId에 대한 재고찰 필요.
  static final String NORMAL_INSERT_QUERY = """
      INSERT INTO logs (
          logtype,
          timestamp,
          level,
          service,
          "class",
          message,
          host,
          container,
          stacktrace,
          traceId,
          spanId,
          parentSpanId,
          log_major_version,
          meta
      )
      VALUES (
          :logtype,
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
          :log_major_version,
          coalesce(:meta::jsonb, '{}'::jsonb)
      ) ON CONFLICT(message_id) DO NOTHING
      """;
  static final String ERROR_INSERT_QUERY = """
      INSERT INTO err_logs (message_id, origin_log, reason, occurred_at, stage, log_version_major)
            VALUES (:message_id, :origin_log, :reason, :occurred_at, :stage, :log_version_major)
            ON CONFLICT(message_id) DO NOTHING
      """;

  private static final Logger log = LoggerFactory.getLogger(PostgresPersistenceAdapter.class);

  private final Jdbi jdbi;
  private final DataSource dataSource;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  PostgresPersistenceAdapter(Jdbi jdbi, DataSource dataSource) {
    this.jdbi = jdbi;
    this.dataSource = dataSource;
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    LogProcessingContext result = new DefaultLogProcessingContext();
    if(!messages.isEmpty()) {
      jdbi.useHandle(handle -> {
        PreparedBatch normalBatch = handle.prepareBatch(NORMAL_INSERT_QUERY);
        PreparedBatch errorBatch = handle.prepareBatch(ERROR_INSERT_QUERY);

        int normalCount = 0;
        int errorCount = 0;

        for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
          LogMessage message = it.next();

          if (message.isError()) {
            for(ErrorLogSchema param : ErrorLogSchema.values()) {
              errorBatch.bind(param.getSchemaName(), message.get(param.getSchemaName()));
            }
            errorBatch.add();
            errorCount++;
          } else {
            // TODO 기능 완성 및 스키마 완성 필요
            normalBatch
                .bind(LOG_TYPE.getSchemaName(),   message.get(StructuredLogFields.LOG_TYPE.getFieldName()))
                .bind(TIMESTAMP.getSchemaName(),  message.get(StructuredLogFields.TIME_STAMP.getFieldName()))
                .bind(LOG_LEVEL.getSchemaName(),  message.get(StructuredLogFields.LEVEL.getFieldName()))
                .bind(SERVICE.getSchemaName(),    message.get(StructuredLogFields.SERVICE.getFieldName()))
                .bind(CLASS_NAME.getSchemaName(), message.get(StructuredLogFields.CLASS.getFieldName()))
                .bind(MESSAGE.getSchemaName(),    message.get(StructuredLogFields.MESSAGE.getFieldName()))
                .bind(HOST.getSchemaName(),       message.get(StructuredLogFields.HOST.getFieldName()))
                .bind(CONTAINER.getSchemaName(),  message.get(StructuredLogFields.CONTAINER.getFieldName()))
                .bind(STACKTRACE.getSchemaName(), message.get(StructuredLogFields.STACK_TRACE.getFieldName()))
                .bind(TRACE_ID.getSchemaName(),   message.get(StructuredLogFields.TRACE_ID.getFieldName()))
                .bind(SPAN_ID.getSchemaName(),    message.get(StructuredLogFields.SPAN_ID.getFieldName()))
                .bind(PARENT_SPAN_ID.getSchemaName(),
                                                  message.get(StructuredLogFields.PARENT_SPAN_ID.getFieldName()))
                .bind(LOG_VERSION_MAJOR.getSchemaName(),
                                                  message.get(LOG_VERSION_MAJOR.getSchemaName()))
                .bind(META.getSchemaName(),       message.get(META.getSchemaName()))
                .add();
            normalCount++;
          }

          result.stackingLogMessage(message);
        }

        if(normalCount > 0) {
          logBatchResult("normal", normalBatch.execute());
        }
        if(errorCount > 0) {
          logBatchResult("error", errorBatch.execute());
        }
      });
    }

    return ProcessResult.success(result);
  }

  /**
   * JDBC 배치 결과(int[])를 요약해서 로그로 남긴다.
   * 규칙:
   *  - 1  : 성공한 행 수 1 (INSERT/UPDATE 성공)
   *  - 0  : 영향 없음 (여기선 ON CONFLICT DO NOTHING 등 멱등 충돌로 간주)
   *  - -2 : Statement.SUCCESS_NO_INFO (성공했으나 행 수 미상) → 성공으로 취급
   *  - -3 : Statement.EXECUTE_FAILED (실패)
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

  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

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

  public static class Builder implements LogProcessorPort.Builder<PostgresPersistenceAdapter> {

    private HikariConfig config;

    @Override
    public LogProcessorPort.Builder<PostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {

      config = getHikariConfigFromSetting(setting);
      return this;
    }

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

    @Override
    public PostgresPersistenceAdapter build() {
      DataSource dataSource = new HikariDataSource(config);
      Jdbi jdbi = Jdbi.create(dataSource);
      return new PostgresPersistenceAdapter(jdbi, dataSource);
    }
  }
}
