package com.rihee.alerting.loggingService.adapter.out.persistence;

import com.rihee.alerting.common.constant.message.StructuredLogProperties;
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
import java.util.Iterator;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

@PersistenceType("postgres")
public final class PostgresPersistenceAdapter extends LogPersistencePort implements AutoCloseable {

  private static final String NORMAL_INSERT_QUERY = """
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
          sampled,
          flags,
          log_major_version,
          call,
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
          :sampled,
          :flags,
          :log_major_version,
          coalesce(:call::jsonb, '{}'::jsonb),
          coalesce(:meta::jsonb, '{}'::jsonb)
      ) ON CONFLICT(message_id) DO NOTHING
      """;
  private static final String ERROR_INSERT_QUERY = """
      INSERT INTO err_logs (message_id, origin_log, reason, occurred_at, stage, log_version_major)
            VALUES (:messageId, :originLog, :reason, :occurred_at, :stage, :log_version_major)
            ON CONFLICT(message_id) DO NOTHING
      """;

  private final Jdbi jdbi;
  private final DataSource dataSource;

  private PostgresPersistenceAdapter(Jdbi jdbi, DataSource dataSource) {
    this.jdbi = jdbi;
    this.dataSource = dataSource;
  }

  PostgresPersistenceAdapter(DataSource dataSource) {
    this.jdbi = Jdbi.create(dataSource);
    this.dataSource = dataSource;
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    LogProcessingContext result = new DefaultLogProcessingContext();
    jdbi.useHandle(handle -> {
      PreparedBatch normalBatch = handle.prepareBatch(NORMAL_INSERT_QUERY);
      PreparedBatch errorBatch = handle.prepareBatch(ERROR_INSERT_QUERY);

      for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
        LogMessage message = it.next();

        if (message.isError()) {
          errorBatch
              .bind("messageId", message.get(ErrorLogSchema.MESSAGE_ID.getSchemaName()))
              .bind("originLog", message.get(ErrorLogSchema.ORIGIN_LOG.getSchemaName()))
              .bind("reason", message.get(ErrorLogSchema.REASON.getSchemaName()))
              .bind("occurred_at", message.get(ErrorLogSchema.OCCURRED_AT.getSchemaName()))
              .bind("stage", message.get(ErrorLogSchema.STAGE.getSchemaName()))
              .bind("log_version_major",
                                      message.get(ErrorLogSchema.LOG_VERSION_MAJOR.getSchemaName()))
              .add();
        } else {
          // TODO 기능 완성 및 스키마 완성 필요
          normalBatch
              .bind("logtype", message.get(StructuredLogProperties.LOG_TYPE.getFieldName()))
              .bind("timestamp", message.get(StructuredLogProperties.TIME_STAMP.getFieldName()))
              .bind("level", message.get(StructuredLogProperties.LEVEL.getFieldName()))
              .bind("service", message.get(StructuredLogProperties.SERVICE.getFieldName()))
              .bind("class", message.get(StructuredLogProperties.CLASS.getFieldName()))
              .bind("message", message.get(StructuredLogProperties.MESSAGE.getFieldName()))
              .bind("host", message.get(StructuredLogProperties.HOST.getFieldName()))
              .bind("container", message.get(StructuredLogProperties.CONTAINER.getFieldName()))
              .bind("stacktrace", message.get(StructuredLogProperties.STACK_TRACE.getFieldName()))
              .bind("traceId", message.get(StructuredLogProperties.TRACE_ID.getFieldName()))
              .bind("spanId", message.get(StructuredLogProperties.SPAN_ID.getFieldName()))
              .bind("parentSpanId",
                                message.get(StructuredLogProperties.PARENT_SPAN_ID.getFieldName()))
              .bind("sampled", message.get(StructuredLogProperties.SAMPLED.getFieldName()))
              .bind("flags", message.get(StructuredLogProperties.FLAGS.getFieldName()))
              .bind("log_major_version",
                                    message.get(NormalLogSchema.LOG_VERSION_MAJOR.getSchemaName()))
              .bind("call", message.get("call"))
              .bind("meta", message.get("meta"))
              .add();
        }

        result.stackingLogMessage(message);
      }

      normalBatch.execute();
      errorBatch.execute();
    });
    return ProcessResult.success(result);
  }

  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public void close() throws Exception {
    if (dataSource instanceof AutoCloseable) {
      try {
        ((AutoCloseable) dataSource).close();
      } catch (Exception ignore) {
        // 오류가 발생하더라도 무시한다. 이미 종료된 자원이거나, 종료할 수 없는 자원임.
      }
    }
  }

  public static class Builder implements LogProcessorPort.Builder<PostgresPersistenceAdapter> {

    private static volatile HikariDataSource dataSource;
    private static volatile Jdbi jdbi;

    @Override
    public LogProcessorPort.Builder<PostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {

      if (jdbi == null) {
        synchronized (Builder.class) {
          if (jdbi == null) {
            HikariConfig config = getHikariConfigFromSetting(setting);
            dataSource = new HikariDataSource(config);
            jdbi = Jdbi.create(dataSource);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
              dataSource.close();
            }));
          }
        }
      }

      return this;
    }

    private static boolean isDevOrTestMode() {
      String programMode = System.getProperty("PROGRAM_MODE");
      if (programMode == null) {
        programMode = System.getenv("PROGRAM_MODE");
      }
      return programMode != null
          && (programMode.equalsIgnoreCase("dev")
          || programMode.equalsIgnoreCase("test"));
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
      return new PostgresPersistenceAdapter(jdbi, dataSource);
    }
  }
}
