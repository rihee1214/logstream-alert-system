package com.rihee.alerting.loggingService.adapter.out.persistence;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.mockito.Answers;
import org.mockito.Mockito;

@PersistenceType("postgres")
public final class TestPostgresPersistenceAdapter extends LogPersistencePort {

  private final PostgresPersistenceAdapter adapter;

  private TestPostgresPersistenceAdapter(PostgresPersistenceAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    return adapter.process(messages);
  }

  @Override
  public void close() throws Exception {
    if (adapter != null) {
      adapter.close();
    }
  }

  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

  public static class Builder implements LogProcessorPort.Builder<TestPostgresPersistenceAdapter> {

    @Override
    public LogProcessorPort.Builder<TestPostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {
      PostgresPersistenceAdapter.builder().withProperties(setting);
      return this;
    }

    @Override
    public TestPostgresPersistenceAdapter build() {
      Jdbi jdbi                 = Mockito.mock(Jdbi.class);
      Handle handle             = Mockito.mock(Handle.class);
      PreparedBatch normalBatch
          = Mockito.mock(PreparedBatch.class,
          Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF));
      PreparedBatch errorBatch
          = Mockito.mock(PreparedBatch.class,
          Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF));

      AtomicInteger normalAdds = new AtomicInteger(0);
      AtomicInteger errorAdds = new AtomicInteger(0);

      Mockito.when(normalBatch.add()).thenAnswer(ans -> {
        normalAdds.incrementAndGet();
        return normalBatch;
      });
      Mockito.when(errorBatch.add()).thenAnswer(ans -> {
        errorAdds.incrementAndGet();
        return errorBatch;
      });

      Mockito.when(normalBatch.execute())
          .thenAnswer(ans -> {
            int n =  normalAdds.get();
            int[] res = new int[n];
            Arrays.fill(res, 1);
            return res;
          });
      Mockito.when(errorBatch.execute())
          .thenAnswer(ans -> {
            int n =  errorAdds.get();
            int[] res = new int[n];
            Arrays.fill(res, 1);
            return res;
          });

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

      DataSource ds = Mockito.mock(
          DataSource.class,
          Mockito.withSettings().extraInterfaces(AutoCloseable.class)
      );

      PostgresPersistenceAdapter adapter = new PostgresPersistenceAdapter(jdbi, ds);
      return new TestPostgresPersistenceAdapter(adapter);
    }
  }
}
