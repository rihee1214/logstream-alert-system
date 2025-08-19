package com.rihee.alerting.loggingService.core.pipeline.result;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;

/**
 * 로그 처리 단계의 결과를 표현하는 불변 데이터 객체입니다.
 *
 * <p>이 객체는 로그 처리 파이프라인 내에서 각 {@link LogProcessor}
 * 가 처리 결과를 명시적으로 반환하기 위해 사용되며, 다음 단계로의 진행 여부 및 커밋 가능 여부를 함께 포함합니다.
 *
 * <p>예외를 직접 던지지 않고 처리 흐름을 제어하고자 할 때 사용됩니다.
 * 예컨대 유효하지 않은 로그이지만 시스템 오류는 아닌 경우,
 * 또는 로그는 처리했지만 consumer offset을 커밋해서는 안 되는 상황 등에서 적절한 제어를 가능하게 합니다.
 *
 * @param context         처리된 로그 메시지 컨텍스트 (절대 null 아님)
 * @param shouldContinue  이후 처리 단계를 계속 진행할지 여부
 * @param shouldCommit    현재 로그 배치를 커밋(ack)해도 되는지 여부
 * @param reason          처리 흐름 제어의 사유 설명 (예: 스킵/에러 원인)
 *
 * @see LogProcessor
 * @see LogProcessingContext
 */
public record ProcessResult(LogProcessingContext context,
                            boolean shouldContinue,
                            boolean shouldCommit,
                            String reason) {

  /**
   * 정상적으로 로그 처리를 완료했으며,
   * 이후 단계로의 전파 및 커밋도 가능한 성공 결과를 반환합니다.
   *
   * @param context 현재까지의 처리 상태를 담은 컨텍스트
   * @return 처리 성공을 나타내는 {@code ProcessResult}
   */
  public static ProcessResult success(LogProcessingContext context) {
    return new ProcessResult(context, true, true, "");
  }

  /**
   * 로그 처리 중 치명적인 오류가 발생했으며,
   * 더 이상의 처리를 중단하고 커밋도 수행하지 않아야 할 경우 사용합니다.
   *
   * @param context 현재까지의 처리 상태를 담은 컨텍스트 (실패한 로그 포함 가능)
   * @param reason  에러 발생 원인 또는 설명
   * @return 처리를 중단하고 커밋도 방지해야 함을 나타내는 {@code ProcessResult}
   */
  public static ProcessResult error(LogProcessingContext context, String reason) {
    return new ProcessResult(context, false, false, reason);
  }
}
