package com.rihee.alerting.loggingService.adapter.out.persistence;

import com.rihee.alerting.loggingService.adapter.in.collector.KafkaLogCollectorAdapter;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.testinfra.common.TestProcessorAdapter;
import com.zaxxer.hikari.HikariDataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.common.TopicPartition;
import org.jdbi.v3.core.Jdbi;

@PersistenceType("postgres")
public final class TestPostgresPersistenceAdapter extends LogPersistencePort
                                                implements TestProcessorAdapter {

  private final Jdbi jdbi;

  private TestPostgresPersistenceAdapter(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {

    return null;
  }

  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public void createNewInstance() {

  }

  @Override
  public void close() throws Exception {

  }

  public static class Builder implements LogProcessorPort.Builder<TestPostgresPersistenceAdapter> {

    private static volatile HikariDataSource dataSource;
    private static volatile Jdbi jdbi;

    @Override
    public LogProcessorPort.Builder<TestPostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {

      return this;
    }

    @Override
    public TestPostgresPersistenceAdapter build() {
      return new TestPostgresPersistenceAdapter(jdbi);
    }
  }

  private class MockDataSource implements DataSource {

    @Override
    public Connection getConnection() throws SQLException {
      return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      return null;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }
  }

  /**
   * 스레드-국소 하네스 리소스 묶음.
   *
   * <p>현재 스레드에서만 접근해야 합니다.
   */
  private record PostgresPersistenceResource(KafkaLogCollectorAdapter adapter,
                                           MockConsumer<String, String> consumer,
                                           List<TopicPartition> topics) {

  }
}
