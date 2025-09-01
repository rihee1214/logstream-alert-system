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

/**
 * {@code LogPersistencePlugin}은 설정 정보를 기반으로
 * {@link LogPersistencePort} 구현체를 동적으로 로딩하고,
 * 해당 구현체의 {@code builder()}를 통해 인스턴스를 생성하는 플러그인입니다.
 *
 * <p>Persistence 구현체는 AnnotationProcessor에 의해 생성되는 클래스패스의 JSON 설정 파일
 * <b>{@code resources/META_INF/logging/PersistenceType.json}</b>에
 * 정의된 <code>persistence.type → FQCN</code> 매핑으로 식별됩니다
 * (경로 상수는
 * {@link com.rihee.alerting.loggingService.tools.constants.ProcessorRegistryPaths#PERSISTENCE} 참조).
 * JSON 파싱은
 * {@link com.rihee.alerting.common.util.MapUtils#fromInputStream(java.io.InputStream)}를 사용합니다.
 *
 * <p>동작 흐름:
 * <ol>
 *   <li>생성자에서 {@code persistence.type} 설정 값을 확인</li>
 *   <li>{@code resources/META_INF/logging/PersistenceType.json}을 읽어 타입에 매핑된 FQCN 조회</li>
 *   <li>리플렉션으로 대상 클래스를 로딩</li>
 *   <li>대상의 {@code public static builder()}를 호출해 {@link LogProcessorPort.Builder} 획득</li>
 *   <li>빌더에 설정 주입 후, 필요 시 {@link #newProcessorInstance()}로 인스턴스 생성</li>
 * </ol>
 *
 * <p><b>예외 처리:</b>
 * <ul>
 *   <li>{@code persistence.type} 누락/공백: {@link IllegalArgumentException}</li>
 *   <li>
 *     JSON 리소스 미존재/파싱 실패, 클래스 로딩 실패, {@code builder()} 미존재/반환 타입 불일치: {@link IllegalStateException}
 *   </li>
 * </ul>
 *
 * @see LogProcessorPlugin
 * @see LogPersistencePort
 * @see LogProcessorPort.Builder
 */
public final class LogPersistencePlugin implements LogProcessorPlugin {

  private final Builder<?> builder;
  private final String persistenceType;

  /**
   * 설정(Map)을 기반으로 {@code LogPersistencePlugin}을 초기화합니다.
   *
   * <p>이 생성자는 다음을 수행합니다:
   * <ol>
   *   <li>{@code persistence.type} 값을 읽어 Persistence 타입 식별</li>
   *   <li>{@link #resolvePersistenceBuilder(String)}를 호출해
   *       <b>classpath: {@code resources/META_INF/logging/PersistenceType.json}</b>에서
   *       타입에 매핑된 FQCN을 찾고, 해당 클래스의 {@code public static builder()}로
   *       {@link LogProcessorPort.Builder}를 획득</li>
   *   <li>획득한 빌더에 {@code setting}을 주입</li>
   * </ol>
   *
   * @param setting 반드시 {@code persistence.type} 키를 포함한 설정 Map
   * @throws IllegalArgumentException {@code persistence.type} 누락/공백인 경우
   * @throws IllegalStateException JSON 리소스 미존재/파싱 실패, 클래스 로딩/빌더 호출 실패 등 내부 로딩 문제
   */
  public LogPersistencePlugin(Map<String, String> setting) {
    this.persistenceType = setting.get("persistence.type");
    if (this.persistenceType == null || this.persistenceType.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'persistence.type' 이 존재하지 않습니다.");
    }

    this.builder = resolvePersistenceBuilder(this.persistenceType)
                            .withProperties(setting);
  }

  /**
   * <b>classpath: {@code resources/META_INF/logging/PersistenceType.json}</b>에서
   * {@code persistenceMode}에 매핑된 FQCN을 조회하고,
   * 해당 클래스의 {@code public static builder()}를 호출해 Builder를 반환합니다.
   *
   * <p>리소스 경로는
   * {@link com.rihee.alerting.loggingService.tools.constants.ProcessorRegistryPaths#PERSISTENCE}
   * 에 정의되며, JSON 파싱은
   * {@link com.rihee.alerting.common.util.MapUtils#fromInputStream(java.io.InputStream)}로 수행됩니다.
   *
   * @param persistenceMode JSON에 정의된 persistence 타입 키 (예: {@code "postgres"})
   * @return 해당 구현체의 {@link LogProcessorPort.Builder}
   * @throws IllegalArgumentException JSON에 타입 키가 없을 때
   * @throws IllegalStateException JSON 리소스 미존재/파싱 실패, 클래스 로딩 실패, {@code builder()} 미존재/호출 실패 등
   */
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
      persistenceClass = Class.forName(fqcn, false, cl);
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

  /**
   * 내부에 보관 중인 {@link LogProcessorPort.Builder}를 사용해
   * 새로운 {@link LogPersistencePort} 구현체의 인스턴스를 생성합니다.
   *
   * @return 새로운 Persistence 프로세서 인스턴스
   */
  public LogProcessorPort newProcessorInstance() {
    return this.builder.build();
  }

  /**
   * 현재 플러그인이 참조하는 persistence 타입
   * (설정의 {@code persistence.type})을 반환합니다.
   *
   * @return persistence 타입 문자열
   */
  public String getProcessorType() {
    return this.persistenceType;
  }
}
