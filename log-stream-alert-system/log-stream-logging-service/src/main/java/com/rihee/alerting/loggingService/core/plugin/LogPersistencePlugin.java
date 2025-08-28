package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort.Builder;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.tools.constants.ProcessorRegistryPaths;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class LogPersistencePlugin implements LogProcessorPlugin {

  private static final String PERSISTENCE_NAMESPACE
                      = "com.rihee.alerting.loggingService.adapter.out.persistence";

  private final Builder<?> builder;
  private final String persistenceType;

  public LogPersistencePlugin(Map<String, String> setting) {
    this.persistenceType = setting.get("persistence.type");
    if (this.persistenceType == null || this.persistenceType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'persistence.type' 이 존재하지 않습니다.");
    }

    this.builder = resolvePersistenceBuilder(this.persistenceType)
                            .withProperties(setting);
  }

  @SuppressWarnings("unchecked")
  private static LogPersistencePort.Builder<?> resolvePersistenceBuilder(String persistenceMode) {
    final String filePath = ProcessorRegistryPaths.PERSISTENCE.getFilePath();
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
    final String fqcn = (String) classInfos.get(persistenceMode);
    if (StringUtils.isBlank(fqcn)) {
      throw new IllegalArgumentException("Unknown persistence mode: " + persistenceMode
          + " (in " + filePath + ")");
    }

    // 3) 클래스 로딩
    final Class<?> persistenceClass;
    try {
      persistenceClass = Class.forName(fqcn, /*initialize*/ false, cl);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Persistence class not found: " + fqcn, e);
    }

    // 4) builder() 확인 및 호출
    try {
      // public static builder()
      Method builderMethod = persistenceClass.getMethod("builder");

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

  public LogProcessorPort newProcessorInstance() {
    return this.builder.build();
  }

  public String getProcessorType() {
    return this.persistenceType;
  }
}
