package com.rihee.alerting.common.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 메모리 Appender<br>
 * 로그 이벤트를 메모리에 쌓아 검증할 수 있도록 지원한다.
 */
public class MemoryAppender extends AppenderBase<ILoggingEvent> {

  private final List<ILoggingEvent> events = new ArrayList<>();

  @Override
  protected void append(ILoggingEvent eventObject) {
    events.add(eventObject);
  }

  public List<ILoggingEvent> getLoggedEvents() {
    return events;
  }

  /**
   * 등록된 모든 로그 이벤트를 정리하는 메서드.
   */
  public void clear() {
    events.clear();
  }
}
