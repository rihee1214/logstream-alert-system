package com.rihee.alerting.loggingService.core.pipeline.context;

import com.rihee.alerting.loggingService.core.model.LogMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link LogProcessingContext}의 기본 구현체입니다.
 *
 * <p>내부적으로는 {@link ArrayList}를 이용해 {@link LogMessage}들을 보관하며,
 * 로그 파이프라인 전 단계에서 공통적으로 사용되는 컨텍스트 객체 역할을 합니다.
 *
 * <p>특징:
 * <ul>
 *   <li>새로운 로그 메시지를 누적({@link #stackingLogMessage(LogMessage)})</li>
 *   <li>누적된 메시지들을 순회할 수 있는 반복자 제공({@link #iterator()})</li>
 *   <li>현재 비어 있는지 여부 확인({@link #isEmpty()})</li>
 * </ul>
 *
 * <p>이 구현체는 파이프라인을 단순하게 테스트하거나, 특별한 최적화가 필요 없는
 * 기본 사용 사례에서 활용할 수 있는 표준 구현입니다.
 *
 * <p><b>스레드 안정성:</b> {@code ArrayList} 기반으로 구현되어 있으므로,
 * 동시 접근이 예상되는 경우 외부에서 동기화 메커니즘을 제공해야 합니다.
 *
 * @see LogProcessingContext
 * @see LogMessage
 */
public class DefaultLogProcessingContext implements LogProcessingContext {

  private final List<LogMessage> logMessages = new ArrayList<>();

  /**
   * 새로운 로그 메시지를 컨텍스트에 추가합니다.
   *
   * @param message 추가할 로그 메시지 (null 불가)
   */
  @Override
  public void stackingLogMessage(LogMessage message) {
    this.logMessages.add(message);
  }

  /**
   * 누적된 로그 메시지들을 순회할 수 있는 반복자를 반환합니다.
   *
   * @return 로그 메시지 반복자
   */
  @Override
  public Iterator<LogMessage> iterator() {
    return logMessages.iterator();
  }

  /**
   * 현재 컨텍스트가 비어 있는지 여부를 반환합니다.
   *
   * @return 로그 메시지가 없으면 {@code true}, 그렇지 않으면 {@code false}
   */
  @Override
  public boolean isEmpty() {
    return logMessages.isEmpty();
  }

  @Override
  public int size() {
    return logMessages.size();
  }

  @Override
  public String toString() {
    return this.logMessages.toString();
  }
}
