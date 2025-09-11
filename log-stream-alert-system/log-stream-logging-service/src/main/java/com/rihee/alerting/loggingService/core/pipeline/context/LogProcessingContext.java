package com.rihee.alerting.loggingService.core.pipeline.context;

import com.rihee.alerting.loggingService.core.model.LogMessage;
import java.util.Iterator;

/**
 * 로그 처리 파이프라인 전 단계에서 공통으로 사용되는 컨텍스트 객체를 정의하는 인터페이스입니다.
 *
 * <p>각 {@link com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort}
 * 구현체들은 이 컨텍스트를 입력받아 처리 결과를 담아 반환하며,
 * 따라서 파이프라인 전체를 관통하는 데이터 컨테이너 역할을 합니다.
 *
 * <p>내부적으로는 {@link LogMessage} 리스트를 보관하며,
 * 수집(collect) → 검증(validate) → 영속화(persist) 등
 * 모든 단계에서 로그 메시지를 누적(stacking)하고 전달할 수 있습니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>{@link #stackingLogMessage(LogMessage)} : 새로운 로그 메시지를 누적</li>
 *   <li>{@link #iterator()} : 누적된 로그 메시지들에 대한 반복자 제공</li>
 *   <li>{@link #isEmpty()} : 현재 컨텍스트가 비어있는지 여부 확인</li>
 * </ul>
 *
 * <p><b>사용 예시:</b>
 * <pre>{@code
 * LogProcessingContext context = ...;
 * context.stackingLogMessage(logMessage);
 *
 * if (!context.isEmpty()) {
 *     for (LogMessage msg : (Iterable<LogMessage>) () -> context.iterator()) {
 *         // 각 로그 메시지 처리
 *     }
 * }
 * }</pre>
 *
 * @see LogMessage
 */
public interface LogProcessingContext {

  /**
   * 새로운 로그 메시지를 현재 컨텍스트에 누적합니다.
   *
   * @param message 추가할 로그 메시지 (null 불가)
   */
  void stackingLogMessage(LogMessage message);

  /**
   * 현재 컨텍스트에 누적된 로그 메시지들을 순회할 수 있는 {@link Iterator}를 반환합니다.
   *
   * @return 로그 메시지 반복자
   */
  Iterator<LogMessage> iterator();

  /**
   * 컨텍스트가 비어있는지 여부를 반환합니다.
   *
   * @return 로그 메시지가 하나도 없으면 {@code true}, 그렇지 않으면 {@code false}
   */
  boolean isEmpty();

  /**
   * 컨텍스트에 담긴 메시지 수를 반환합니다.
   * 내부적으로 관리되는 리스트의 크기와 같습니다.
   *
   * @return 메시지 개수
   */
  int size();
}
