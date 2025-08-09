package com.rihee.alerting.loggingService.persistence.impl;

import com.rihee.alerting.common.constant.storage.ErrorLogSchema;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Iterator;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

@PersistenceType("postgres")
public final class PostgresPersistence extends LogPersistence {

  private static final String NORMAL_INSERT_QUERY = """
      INSERT INTO logs (trace_id, level, message, timestamp)
            VALUES (:traceId, :level, :message, :timestamp)
            ON CONFLICT(trace_id, )
            DO NOTHING
      """;
  private static final String ERROR_INSERT_QUERY = """
      INSERT INTO err_logs (message_id, origin_log, reason, log_version_major)
            VALUES (:messageId, :originLog, :reason, :log_version_major)
            ON CONFLICT(message_id)
            DO NOTHING
      """;

  private final Jdbi jdbi;

  private PostgresPersistence(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    LogProcessingContext result = new DefaultLogProcessingContext();
    jdbi.useHandle(handle -> {
      handle.createBatch();

      PreparedBatch normalBatch = handle.prepareBatch(NORMAL_INSERT_QUERY);
      PreparedBatch errorBatch = handle.prepareBatch(ERROR_INSERT_QUERY);

      for (Iterator<LogMessage> it = messages.iterator(); it.hasNext();) {
        LogMessage message = it.next();

        if (message.isError()) {
          normalBatch
              .bind("traceId", message.get(""))
              .bind("level", message.get(""))
              .bind("message", message.get(""))
              .bind("timestamp", message.get(""))
              .add();
        } else {
          errorBatch
              .bind("messageId", message.get(ErrorLogSchema.MESSAGE_ID.getSchemaName()))
              .bind("originLog", message.get(ErrorLogSchema.ORIGIN_LOG.getSchemaName()))
              .bind("reason", message.get(ErrorLogSchema.REASON.getSchemaName()))
              .add();
        }

        result.stackingLogMessage(message);
      }

      normalBatch.execute();
      errorBatch.execute();
    });
    return ProcessResult.success(result);
  }

  public static LogProcessor.Builder<?> builder() {
    return new Builder();
  }

  public static class Builder implements LogProcessor.Builder<PostgresPersistence> {

    private static volatile HikariDataSource dataSource;
    private static volatile Jdbi jdbi;

    @Override
    public LogProcessor.Builder<PostgresPersistence>
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
    public PostgresPersistence build() {
      return new PostgresPersistence(jdbi);
    }
  }
}
