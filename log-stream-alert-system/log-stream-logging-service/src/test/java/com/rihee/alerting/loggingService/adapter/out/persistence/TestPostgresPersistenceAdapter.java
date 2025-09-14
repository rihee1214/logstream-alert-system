package com.rihee.alerting.loggingService.adapter.out.persistence;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.testinfra.common.TestProcessorAdapter;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.mockito.Answers;
import org.mockito.Mockito;

@PersistenceType("postgres")
public final class TestPostgresPersistenceAdapter extends LogPersistencePort
                                                implements TestProcessorAdapter {

  private static final ThreadLocal<PostgresPersistenceAdapter> HARNESS_THREAD_LOCAL
                                                                            = new ThreadLocal<>();

  private PostgresPersistenceAdapter requireHarness() {
    var sut = HARNESS_THREAD_LOCAL.get();
    if (sut == null) {
      throw new IllegalStateException("createNewInstance() 먼저 호출하세요.");
    }
    return sut;
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    return requireHarness().process(messages);
  }

  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public void createNewInstance() {
    var old = HARNESS_THREAD_LOCAL.get();
    if (old != null) {
      try {
        old.close();
      } catch (Exception ignore) {
        // 자원 종료시 문제가 생겨도 조용히 끝내기
      }
      HARNESS_THREAD_LOCAL.remove();
    }

    Jdbi jdbi                 = Mockito.mock(Jdbi.class);
    Handle handle             = Mockito.mock(Handle.class);
    PreparedBatch normalBatch
        = Mockito.mock(PreparedBatch.class,
        Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF));
    PreparedBatch errorBatch
        = Mockito.mock(PreparedBatch.class,
        Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF));

    Mockito.doAnswer(ans -> {
      @SuppressWarnings("unchecked")
      var consumer = (HandleConsumer<RuntimeException>) ans.getArgument(0);
      consumer.useHandle(handle);
      return null;
    }).when(jdbi).useHandle(Mockito.any());

    Mockito.when(
        handle.prepareBatch(PostgresPersistenceAdapter.NORMAL_INSERT_QUERY)
    ).thenReturn(normalBatch);
    Mockito.when(
        handle.prepareBatch(PostgresPersistenceAdapter.ERROR_INSERT_QUERY)
    ).thenReturn(errorBatch);

    Mockito.when(normalBatch.execute()).thenReturn(new int[0]);
    Mockito.when(errorBatch.execute()).thenReturn(new int[0]);

    HARNESS_THREAD_LOCAL.set(new PostgresPersistenceAdapter(jdbi, Mockito.mock(DataSource.class)));
  }

  @Override
  public void close() throws Exception {
    PostgresPersistenceAdapter r = HARNESS_THREAD_LOCAL.get();
    try {
      if (r != null) {
        r.close(); // adapter.close() 호출
      }
    } catch (Exception ignore) {
      // 테스트 하네스라 조용히 무시
    } finally {
      HARNESS_THREAD_LOCAL.remove();
    }
  }

  public static class Builder implements LogProcessorPort.Builder<TestPostgresPersistenceAdapter> {

    @Override
    public LogProcessorPort.Builder<TestPostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {

      return this;
    }

    @Override
    public TestPostgresPersistenceAdapter build() {
      return new TestPostgresPersistenceAdapter();
    }
  }
}
