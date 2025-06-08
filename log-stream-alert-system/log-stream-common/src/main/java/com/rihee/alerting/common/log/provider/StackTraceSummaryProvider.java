package com.rihee.alerting.common.log.provider;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.AbstractJsonProvider;

public class StackTraceSummaryProvider extends AbstractJsonProvider<ILoggingEvent> {

  private int maxDepth = 5;

  @Override
  public void writeTo(JsonGenerator jsonGenerator, ILoggingEvent iLoggingEvent) throws IOException {
    IThrowableProxy throwableProxy = iLoggingEvent.getThrowableProxy();
    if (throwableProxy instanceof ThrowableProxy) {
      Throwable t = ((ThrowableProxy) throwableProxy).getThrowable();
      StackTraceElement[] stackTrace = t.getStackTrace();
      jsonGenerator.writeArrayFieldStart("stacktrace");
      for (int i = 0; i < Math.min(maxDepth, stackTrace.length); i++) {
        jsonGenerator.writeString(stackTrace[i].toString());
      }
      jsonGenerator.writeEndArray();
    }
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }
}
