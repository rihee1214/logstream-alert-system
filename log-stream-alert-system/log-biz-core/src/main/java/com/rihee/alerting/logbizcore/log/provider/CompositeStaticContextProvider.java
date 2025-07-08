package com.rihee.alerting.logbizcore.log.provider;

import static com.rihee.alerting.common.constant.log.StructuredLogProperties.CONTAINER;
import static com.rihee.alerting.common.constant.log.StructuredLogProperties.HOST;
import static com.rihee.alerting.common.constant.log.StructuredLogProperties.LOG_TYPE;
import static com.rihee.alerting.common.constant.log.StructuredLogProperties.SERVICE;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.rihee.alerting.common.constant.log.LogType;
import java.io.IOException;
import java.util.Map;
import net.logstash.logback.composite.AbstractJsonProvider;
import org.springframework.util.StringUtils;

/**
 * 커스텀 정적 필드 Provider 클래스입니다.
 *
 * <p>이 클래스는 시스템 및 환경변수에서 값을 읽어와,
 * JSON 로그에 "service", "host", "container" 필드를 추가합니다.
 * </p>
 *
 * <p>이 Class는 logback-spring.xml에 직접적으로 사용되는 Provider Class 입니다.</p>
 */
public class CompositeStaticContextProvider extends AbstractJsonProvider<ILoggingEvent> {

  /**
   * 로그에 포함될 서비스 이름(예: user-service).
   */
  private static final String serviceName;
  /**
   * 로그에 포함될 호스트 이름(예: mockup-host).
   */
  private static final String hostName = resolveEnv("HOST");
  /**
   * 로그에 포함될 컨테이너 이름 (예: mockup-container-1).
   */
  private static final String containerName = resolveEnv("CONTAINER");

  private static String resolveEnv(String key) {
    String value = System.getenv(key);

    if (!StringUtils.hasText(value)) {
      String message = switch (key) {
        case "HOST" -> "Missing required environment variable: 'HOST'. "
            + "Please set HOST to identify log origin.";
        case "CONTAINER" -> "Missing required environment variable: 'CONTAINER'. "
            + "Please set CONTAINER to distinguish container instance in logs.";
        default -> "Missing required environment variable: '" + key + "'. "
            + "Please ensure it is configured.";
      };
      throw new IllegalStateException(message);
    }

    return value;
  }

  static {
    serviceName = System.getProperty("service.name");
    if (!StringUtils.hasText(serviceName)) {
      throw new IllegalStateException(
          "Missing required configuration: 'service.name'. "
              + "Please set it using -Dservice.name or Environment Variable."
      );
    }
  }

  @Override
  public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
    Map<String, String> mdc = event.getMDCPropertyMap();
    writeIfAbsentInMdc(mdc, generator, SERVICE.getName(), serviceName);
    writeIfAbsentInMdc(mdc, generator, HOST.getName(), hostName);
    writeIfAbsentInMdc(mdc, generator, CONTAINER.getName(), containerName);
    writeIfAbsentInMdc(mdc, generator, LOG_TYPE.getName(), LogType.SYS.getCode());
  }

  /**
   * MDC에 동일한 키가 존재하지 않는 경우에만 JSON 로그에 필드를 추가합니다.
   *
   * <p>중복된 로그 필드를 방지하기 위한 보호 로직으로, StructuredLogger 또는 MDC 설정에서
   * 이미 삽입된 값과 충돌하지 않도록 하기 위해 사용됩니다.</p>
   *
   * @param mdc 현재 로그 이벤트의 MDC 맵
   * @param generator JSON 로그 출력용 생성기
   * @param key 출력할 필드의 키
   * @param value 출력할 필드의 값
   * @throws IOException JSON 작성 중 I/O 오류가 발생한 경우
   */
  private void writeIfAbsentInMdc(Map<String, String> mdc,
                                  JsonGenerator generator,
                                  String key,
                                  String value) throws IOException {
    if (!mdc.containsKey(key)) {
      generator.writeStringField(key, value);
    }
  }
}
