package com.rihee.alerting.loggingService.adapter.rule.validator;

import com.rihee.alerting.common.constant.annotation.LogPolicy;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import com.rihee.alerting.loggingService.testinfra.common.TestProcessorAdapter;
import java.util.Map;

@ValidatorType("default")
public final class TestDefaultLogValidatorAdapter extends LogValidatorPort
                                                    implements TestProcessorAdapter {

  private static final DefaultLogValidatorAdapter ADAPTER = new DefaultLogValidatorAdapter();

  private TestDefaultLogValidatorAdapter() {
  }

  /**
   * {@link TestDefaultLogValidatorAdapter}를 생성하기 위한 빌더를 반환합니다.
   *
   * @return {@link TestDefaultLogValidatorAdapter} 전용 빌더
   */
  public static LogValidatorPort.Builder<?> builder() {
    return new Builder();
  }

  @Override
  public ProcessResult process(LogProcessingContext messages) {
    return ADAPTER.process(messages);
  }

  @Override
  public void createNewInstance() {
    // 초기화 시킬 자원자체가 없기 때문에 무시
  }

  @Override
  public void close() throws Exception {
    // 초기화 시킨 자원자체가 없기 때문에 무시
  }

  /**
   * {@link TestDefaultLogValidatorAdapter} 인스턴스를 생성하기 위한 빌더입니다.
   *
   * <p>현재 구현에서는 외부 설정을 사용하지 않으며,
   * 어노테이션 {@link LogPolicy#isEssential()} 기반 필수 필드만 검증합니다.
   *
   * @see #withProperties(Map)
   * @see #build()
   */
  public static class Builder implements LogValidatorPort.Builder<TestDefaultLogValidatorAdapter> {

    /**
     * 빌더에 설정 값을 전달합니다.
     *
     * @param setting 설정 값 맵(현재 미사용)
     * @return 이 빌더 자신(메서드 체이닝용)
     */
    @Override
    public LogValidatorPort.Builder<TestDefaultLogValidatorAdapter>
                                                withProperties(Map<String, String> setting) {
      return this;
    }

    /**
     * {@link TestDefaultLogValidatorAdapter} 인스턴스를 생성합니다.
     *
     * @return 새 {@link TestDefaultLogValidatorAdapter} 인스턴스
     */
    @Override
    public TestDefaultLogValidatorAdapter build() {
      return new TestDefaultLogValidatorAdapter();
    }
  }
}
