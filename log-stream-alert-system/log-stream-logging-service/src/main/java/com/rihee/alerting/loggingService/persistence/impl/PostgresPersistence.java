package com.rihee.alerting.loggingService.persistence.impl;

import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;

@PersistenceType("postgres")
public final class PostgresPersistence extends LogPersistence {

  private final HikariDataSource dataSource;

  private PostgresPersistence(HikariDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public LogProcessingContext process(LogProcessingContext messages) {
    return messages;
  }

  public static LogProcessor.Builder<?> builder() {
    return new Builder();
  }

  public static class Builder implements LogProcessor.Builder<PostgresPersistence> {


    private static volatile HikariDataSource dataSource;

    @Override
    public LogProcessor.Builder<PostgresPersistence>
                                            withProperties(Map<String, String> setting) {

      if (dataSource == null) {
        synchronized (Builder.class) {
          if (dataSource == null) {
            HikariConfig config = getHikariConfigFromSetting(setting);
            dataSource = new HikariDataSource(config);
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

      // PostgreSQL에서 권장하는 드라이버 설정
      config.setDriverClassName("org.postgresql.Driver");
      return config;
    }



    @Override
    public PostgresPersistence build() {
      return new PostgresPersistence(dataSource);
    }
  }
}
