package com.rihee.alerting.loggingService.test.unit.persistence;

import static com.rihee.alerting.common.constant.logging.StructuredLogFields.CLASS;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.CONTAINER;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.HOST;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.LEVEL;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.LOG_TYPE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.MESSAGE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.NAME;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.PARENT_SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.SERVICE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.STACK_TRACE;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.TIME_STAMP;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.TRACE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.rihee.alerting.loggingService.adapter.out.persistence.TestPostgresPersistenceAdapter;
import com.rihee.alerting.loggingService.core.model.LogErrorMessage;
import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.model.LogNormalMessage;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.runtime.RuntimeBootstrapExtension;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PostgreSQL 영속 어댑터(테스트용 래퍼 {@link TestPostgresPersistenceAdapter})의 단일 메시지 처리 동작을 단위 수준에서 검증한다.
 *
 * <p>실제 DB 연결 없이 {@code TestPostgresPersistenceAdapter}가 주입하는 mock(Jdbi/Handle/Batch)
 * 동작을 통해 파이프라인의 커밋/전달(semanitcs)을 확인한다.
 *
 * <h2>검증 관점</h2>
 * <ul>
 *   <li>정상 로그({@link LogNormalMessage})가 영속 단계에서
 *       <em>계속 진행(shouldContinue=true)</em> 및 <em>커밋 대상(shouldCommit=true)</em>으로
 *       반환되는지</li>
 *   <li>에러 로그({@link LogErrorMessage})도 동일하게 커밋/전달 규약을 만족하는지</li>
 *   <li>파이프라인 컨텍스트에 <em>동일 인스턴스</em>가 유지되는지(불필요한 복제/변형 없음)</li>
 * </ul>
 *
 * <h2>주요 전제</h2>
 * <ul>
 *   <li>{@link RuntimeBootstrapExtension} 확장을 통해 테스트 픽스처(어댑터 인스턴스)가
 *       매 테스트 메서드 인자로 주입된다.</li>
 *   <li>{@link #TEST_PARAM_MAP} 은 정상 로그 생성에 필요한 최소 스키마를 제공한다.</li>
 * </ul>
 *
 * @see TestPostgresPersistenceAdapter
 * @see com.rihee.alerting.loggingService.adapter.out.persistence.PostgresPersistenceAdapter
 * @since 1.0
 */
@ExtendWith(RuntimeBootstrapExtension.class)
public class PostgresPersistenceSingleMessageTest {

  /**
   * 정상 로그 1건을 영속 어댑터로 전달했을 때,
   * 파이프라인이 계속 진행되고(shouldContinue), 커밋 대상으로 표시되며(shouldCommit),
   * 컨텍스트에 동일 객체가 유지되는지(동일 참조) 검증한다.
   */
  @Test
  @DisplayName("단일 정상 메시지를 받아서 해당 메시지를 저장한 후 다음 파이프라인으로 넘긴다.")
  void persistence_single_message_and_emits_to_pipeline(TestPostgresPersistenceAdapter adapter) {

    Map<String, Object> params = new HashMap<>(TEST_PARAM_MAP);

    LogMessage message = LogNormalMessage.fromOriginMessage(params, "abc");
    LogProcessingContext context = new DefaultLogProcessingContext();
    context.stackingLogMessage(message);

    // when: 검증기 처리
    ProcessResult result = adapter.process(context);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);

    LogMessage resultMessage = result.context().iterator().next();
    assertThat(resultMessage.isError()).isFalse();
    assertThat(resultMessage).isInstanceOf(LogNormalMessage.class);
    assertThat(resultMessage).isSameAs(message);
  }

  /**
   * 정상 로그 1건을 영속 어댑터로 전달했을 때,
   * 파이프라인이 계속 진행되고(shouldContinue), 커밋 대상으로 표시되며(shouldCommit),
   * 컨텍스트에 동일 객체가 유지되는지(동일 참조) 검증한다.
   */
  @Test
  @DisplayName("단일 에러 메시지를 받아서 해당 메시지를 저장한 후 다음 파이프라인으로 넘긴다.")
  void persistence_single_error_message_and_emits_to_pipeline(
                                                TestPostgresPersistenceAdapter adapter) {

    Map<String, Object> params = new HashMap<>(TEST_PARAM_MAP);
    params.remove(SERVICE.getFieldName());

    LogMessage origin = LogNormalMessage.fromOriginMessage(params, "abc");
    LogMessage message = LogErrorMessage.fromNormalMessage(origin, "테스트용 에러 발생", "collector");

    LogProcessingContext context = new DefaultLogProcessingContext();
    context.stackingLogMessage(message);

    // when: 검증기 처리
    ProcessResult result = adapter.process(context);

    // then: 처리 결과 및 전달 확인
    assertThat(result.shouldContinue()).isTrue();
    assertThat(result.shouldCommit()).isTrue();
    assertThat(result.context().size()).isEqualTo(1);

    LogMessage resultMessage = result.context().iterator().next();
    assertThat(resultMessage.isError()).isTrue();
    assertThat(resultMessage).isInstanceOf(LogErrorMessage.class);
    assertThat(resultMessage).isSameAs(message);
  }

  private static final Map<String, Object> TEST_PARAM_MAP;

  static {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(LOG_TYPE.getFieldName(), "biz");
    paramMap.put(TIME_STAMP.getFieldName(), Instant.now().toString());
    paramMap.put(SERVICE.getFieldName(), "orders");
    paramMap.put(LEVEL.getFieldName(), "debug");
    paramMap.put(NAME.getFieldName(), "test");
    paramMap.put(CLASS.getFieldName(), "com.example.OrderService"); // 컬럼명은 "class"
    paramMap.put(MESSAGE.getFieldName(), "hello world");
    paramMap.put(HOST.getFieldName(), "ip-10-0-0-1");
    paramMap.put(CONTAINER.getFieldName(), "orders-0");
    paramMap.put(STACK_TRACE.getFieldName(), null);          // 없으면 null
    paramMap.put(TRACE_ID.getFieldName(), "abc123");
    paramMap.put(SPAN_ID.getFieldName(), "def456");
    paramMap.put(PARENT_SPAN_ID.getFieldName(), null);
    TEST_PARAM_MAP = Collections.unmodifiableMap(paramMap);
  }
}
