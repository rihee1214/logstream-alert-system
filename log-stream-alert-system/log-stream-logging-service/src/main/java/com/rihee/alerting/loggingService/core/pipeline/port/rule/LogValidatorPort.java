package com.rihee.alerting.loggingService.core.pipeline.port.rule;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;

/**
 * {@code LogValidator}는 {@link LogProcessorPort} 파이프라인의
 * 한 단계로서, {@link LogProcessingContext}에 담긴 로그 데이터를
 * 유효성 검증하기 위한 추상 클래스입니다.
 *
 * <p>구현체는 {@link #process(LogProcessingContext)} 메서드를 통해
 * 다음과 같은 검증 작업을 수행해야 합니다:
 * <ul>
 *   <li>필수 필드 존재 여부 확인</li>
 *   <li>필드 값의 형식 및 범위 검증</li>
 *   <li>비즈니스 규칙에 따른 데이터 무결성 검사</li>
 * </ul>
 *
 * <p>검증 실패 시 {@code IllegalArgumentException} 또는
 * 서비스 전용 예외를 발생시켜 후속 처리 단계를 중단시킬 수 있습니다.
 * 모든 검증 로직은 가능한 한 상태를 변경하지 않는 방식(stateless)으로 작성해야 합니다.
 *
 * @see LogProcessorPort
 * @see LogProcessingContext
 */
public abstract class LogValidatorPort implements LogProcessorPort {

  private static final String STAGE = "Validator";

  @Override
  public final String stage() {
    return STAGE;
  }

  @Override
  public abstract ProcessResult process(LogProcessingContext processingContext);

}
