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

/**
 * 테스트 전용 래퍼 어댑터.
 *
 * <p>원본 {@link PostgresPersistenceAdapter}를 감싸되, 테스트 환경에서
 * <b>테스트 어노테이션(확장)</b>을 통해 본 클래스로 <em>리다이렉트</em>하여
 * {@link Jdbi}, {@link Handle}, {@link PreparedBatch}, {@link DataSource}를
 * 목(mock)으로 주입하기 위한 용도입니다. 운영 환경에서는 사용하지 않습니다.
 * 상세한 설계/배경은 원본 어댑터 문서를 참고하세요.</p>
 *
 * @see PostgresPersistenceAdapter
 */
@PersistenceType("postgres")
public final class TestPostgresPersistenceAdapter extends LogPersistencePort {

  private final PostgresPersistenceAdapter adapter;

  /**
   * 원본 어댑터를 감싸는 생성자.
   *
   * @param adapter 테스트에서 사용할 위임 대상(원본) 어댑터
   */
  private TestPostgresPersistenceAdapter(PostgresPersistenceAdapter adapter) {
    this.adapter = adapter;
  }

  /**
   * 파이프라인 처리 위임.
   *
   * <p>테스트에서는 빌더에서 구성된 목 객체들이 내부 위임 어댑터에서 사용되며,
   * 실제 DB I/O 없이 로직 경로만 검증됩니다.</p>
   *
   * @param messages 처리 대상 컨텍스트
   * @return 다음 단계 진행 여부/커밋 여부를 포함한 처리 결과
   */
  @Override
  public ProcessResult process(LogProcessingContext messages) {
    return adapter.process(messages);
  }

  /**
   * 자원 정리 위임.
   *
   * <p>감싼 원본 어댑터가 보유한 자원이 있을 경우 정리를 위임합니다.</p>
   */
  @Override
  public void close() throws Exception {
    if (adapter != null) {
      adapter.close();
    }
  }

  /**
   * 테스트 어댑터 생성을 위한 빌더를 반환합니다.
   *
   * @return 테스트 전용 빌더
   */
  public static LogProcessorPort.Builder<?> builder() {
    return new Builder();
  }

  /**
   * 테스트 주입 전용 빌더.
   *
   * <p>테스트 어노테이션(확장) 경로에서 사용되며, 목 객체를 구성해
   * 원본 {@link PostgresPersistenceAdapter}에 주입한 뒤 본 래퍼를 생성합니다.</p>
   */
  public static class Builder implements LogProcessorPort.Builder<TestPostgresPersistenceAdapter> {

    /**
     * 설정 전달(검증) 단계.
     *
     * <p>본 메서드는 <b>해당 테스트 프로세스에서 단 한 번</b> 실행되는
     * 검증/전달 용도로 사용됩니다. 전달된 설정은 내부 원본 어댑터의 빌더에
     * 위임되며, 이 빌더 인스턴스 자체의 상태를 변경하거나 부수효과를 유발하지
     * 않습니다.</p>
     *
     * @param setting 테스트에서 사용할 속성 맵
     * @return 이 빌더 자신
     */
    @Override
    public LogProcessorPort.Builder<TestPostgresPersistenceAdapter>
                                            withProperties(Map<String, String> setting) {
      PostgresPersistenceAdapter.builder().withProperties(setting);
      return this;
    }

    /**
     * 테스트 어댑터 인스턴스를 생성합니다.
     *
     * <p>{@link Jdbi}, {@link Handle}, {@link PreparedBatch}, {@link DataSource}를
     * Mockito로 목킹하고, 정상/에러 배치 쿼리에 대해 서로 다른 배치를 반환하도록
     * 스텁합니다. 이후 목 객체가 주입된 원본 어댑터를 감싸
     * {@link TestPostgresPersistenceAdapter}를 반환합니다.</p>
     *
     * @return 테스트 전용 래퍼 어댑터
     */
    @Override
    public TestPostgresPersistenceAdapter build() {
      // 실제 자원에 대한 mockup객체 생성
      Jdbi jdbi                 = Mockito.mock(Jdbi.class);
      Handle handle             = Mockito.mock(Handle.class);

      // jdbi Batch mockup생성.
      PreparedBatch normalBatch
          = Mockito.mock(PreparedBatch.class,
          Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF));
      PreparedBatch errorBatch
          = Mockito.mock(PreparedBatch.class,
          Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF));

      // jdbiBatch에 데이터 추가시, 카운팅. 결과값 생성에 사용.
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

      // 실제 DB가 없어도, jdbi 흐름을 유지하도록 하기 위한 stub.
      Mockito.doAnswer(ans -> {
        @SuppressWarnings("unchecked")
        var consumer = (HandleConsumer<RuntimeException>) ans.getArgument(0);
        consumer.useHandle(handle);
        return null;
      }).when(jdbi).useHandle(Mockito.any());

      // 에러, 정상 상황에서 어떤 SQL을 사용할지 지정하기 위한 세팅.
      Mockito.when(
          handle.prepareBatch(PostgresPersistenceAdapter.NORMAL_INSERT_QUERY)
      ).thenReturn(normalBatch);
      Mockito.when(
          handle.prepareBatch(PostgresPersistenceAdapter.ERROR_INSERT_QUERY)
      ).thenReturn(errorBatch);

      // DataSource mock객체 생성.
      DataSource ds = Mockito.mock(
          DataSource.class,
          Mockito.withSettings().extraInterfaces(AutoCloseable.class)
      );

      // 위에서 만들어낸 mockup 객체를 원본 adapter코드에 주입 및 TestAdapter에 주입하여 return.
      PostgresPersistenceAdapter adapter = new PostgresPersistenceAdapter(jdbi, ds);
      return new TestPostgresPersistenceAdapter(adapter);
    }
  }
}
