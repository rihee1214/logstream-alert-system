package com.rihee.alerting.common.log.provider;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.AbstractJsonProvider;
import org.springframework.util.StringUtils;

/**
 * 커스텀 정적 필드 Provider 클래스입니다.
 *
 * <p>이 클래스는 시스템 및 환경변수에서 값을 읽어와,
 * JSON 로그에 "service", "host", "container" 필드를 추가합니다.
 * </p>
 */
public class CompositeStaticContextProvider extends AbstractJsonProvider<ILoggingEvent> {

  /**
   * 로그에 포함될 서비스 이름(예: user-service).
   */
  private static final String serviceName = System.getProperty("service.name", "unknown-service");
  /**
   * 로그에 포함될 호스트 이름(예: mockup-host).
   */
  private static final String hostName = resolveEnv("HOST", "unknown-host");
  /**
   * 로그에 포함될 컨테이너 이름 (예: mockup-container-1)
   */
  private static final String containerName = resolveEnv("CONTAINER", "unknown-container");

  private static String resolveEnv(String key, String defaultValue) {
    String value = System.getenv(key);
    return StringUtils.hasText(value) ? value : defaultValue;
  }

  @Override
  public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
    generator.writeStringField("service", serviceName);
    generator.writeStringField("host", hostName);
    generator.writeStringField("container", containerName);
  }
}
