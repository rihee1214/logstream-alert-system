package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort.Builder;
import com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort;
import com.rihee.alerting.loggingService.toos.constants.ProcessorRegistryPaths;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@code LogCollectorPlugin}은 설정 정보를 기반으로 {@link LogCollectorPort}의 구체 구현체를
 * 동적으로 탐색하고, 해당 구현체의 Builder를 통해 인스턴스를 생성하는 책임을 갖는다.
 *
 * <p>Collector는 {@link CollectorType} 애노테이션으로 타입을 식별하며,
 * {@code collector.type} 설정 값을 기준으로 일치하는 클래스를 탐색한다.
 * 탐색된 클래스는 반드시 {@code public static builder()} 메서드를 제공해야 하며,
 * 해당 메서드를 통해 {@link LogProcessorPort.Builder} 인스턴스를
 * 획득하여 설정을 주입하고 Collector를 생성한다.
 *
 * <p>이 클래스는 초기화 시 1회의 설정만 받고, 이후 Collector 인스턴스를 반복 생성할 수 있도록 설계되었다.
 *
 * @author 리희
 * @since 1.0
 */
public final class LogCollectorPlugin implements LogProcessorPlugin {

  private final Builder<?> builder;
  private final String collectorType;

  public LogCollectorPlugin(Map<String, String> setting) {
    this.collectorType = setting.get("collector.type");
    if (this.collectorType == null || this.collectorType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'collector.type' 이 존재하지 않습니다.");
    }

    this.builder = resolveCollectorBuilder(collectorType)
                                  .withProperties(setting);
  }

  @Override
  public LogProcessorPort newProcessorInstance() {
    return builder.build();
  }

  private static LogProcessorPort.Builder<?> resolveCollectorBuilder(String collectorMode) {
    final String filePath = ProcessorRegistryPaths.COLLECTOR.getFilePath();
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();

    // 1) 레지스트리 로딩
    final Map<String, Object> classInfos;
    try (InputStream is = cl.getResourceAsStream(filePath)) {
      if (is == null) {
        throw new IllegalStateException("Registry resource not found: " + filePath);
      }
      // Map<String, ?> 대신 문자열 맵 강제 (JSON 규약: 키는 문자열)
      classInfos = MapUtils.fromInputStream(is);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read registry: " + filePath, e);
    }

    // 2) 대상 FQCN 조회
    final String fqcn = (String) classInfos.get(collectorMode);
    if (StringUtils.isBlank(fqcn)) {
      throw new IllegalArgumentException("Unknown collector mode: " + collectorMode
          + " (in " + filePath + ")");
    }

    // 3) 클래스 로딩
    final Class<?> collectorClass;
    try {
      collectorClass = Class.forName(fqcn, /*initialize*/ false, cl);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Collector class not found: " + fqcn, e);
    }

    // 4) builder() 확인 및 호출
    try {
      // public static builder()
      Method builderMethod = collectorClass.getMethod("builder");

      if (!LogProcessorPort.Builder.class.isAssignableFrom(builderMethod.getReturnType())) {
        throw new IllegalStateException(fqcn + ".builder() must return LogProcessorPort.Builder");
      }

      return (LogProcessorPort.Builder<?>) builderMethod.invoke(null);

    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Missing public static builder() in " + fqcn, e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Failed to invoke builder() in " + fqcn, e);
    }
  }

  public String getProcessorType() {
    return this.collectorType;
  }
}
