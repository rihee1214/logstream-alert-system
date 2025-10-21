package com.rihee.alerting.logbizcore.log.provider;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.rihee.alerting.common.constant.logging.StructuredLogFields;
import java.io.IOException;
import net.logstash.logback.composite.AbstractJsonProvider;

/**
 * 예외가 포함된 로그 이벤트에서 스택트레이스의 앞부분만 추려
 * JSON 필드({@link StructuredLogFields#STACKTRACE})로 쓰는 간단한 Provider입니다.
 *
 * <p>전체 스택트레이스를 모두 출력하지 않고, 최대 {@code maxDepth}개 프레임만 기록하여
 * 로그 용량을 줄이고 가독성을 높이는 데 목적이 있습니다.</p>
 */
public class StackTraceSummaryProvider extends AbstractJsonProvider<ILoggingEvent> {

  /**
   * 기록할 최대 스택 프레임 수. 기본값은 5입니다.
   */
  private int maxDepth = 5;

  /**
   * 예외가 있는 경우 스택트레이스의 상위 {@code maxDepth}개 프레임을
   * {@code STACKTRACE} 배열 필드로 출력합니다.
   * 예외가 없거나 {@link ThrowableProxy}가 아니면 아무 것도 쓰지 않습니다.
   *
   * @param jsonGenerator Logstash가 제공하는 Jackson 제너레이터
   * @param loggingEvent  현재 로깅 이벤트
   * @throws IOException JSON 출력 중 오류가 발생한 경우
   */
  @Override
  public void writeTo(JsonGenerator jsonGenerator, ILoggingEvent loggingEvent) throws IOException {
    IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
    if (throwableProxy instanceof ThrowableProxy) {
      Throwable t = ((ThrowableProxy) throwableProxy).getThrowable();
      StackTraceElement[] stackTrace = t.getStackTrace();
      jsonGenerator.writeArrayFieldStart(StructuredLogFields.STACKTRACE.getFieldName());
      for (int i = 0; i < Math.min(maxDepth, stackTrace.length); i++) {
        jsonGenerator.writeString(stackTrace[i].toString());
      }
      jsonGenerator.writeEndArray();
    }
  }
}
