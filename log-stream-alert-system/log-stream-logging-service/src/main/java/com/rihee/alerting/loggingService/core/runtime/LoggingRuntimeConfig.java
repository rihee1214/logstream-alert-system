package com.rihee.alerting.loggingService.core.runtime;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort;
import com.rihee.alerting.loggingService.core.plugin.LogCollectorPlugin;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.plugin.LogProcessorPlugin;
import com.rihee.alerting.loggingService.core.plan.LogProcessorPluginPlanner;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.core.plugin.LogPersistencePlugin;
import com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort;
import com.rihee.alerting.loggingService.core.plugin.LogValidatorPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * {@code LoggingRuntimeConfig}는 로그 수집 시스템의 런타임 구성 정보를 캡슐화한 설정 객체입니다.
 *
 * <p>이 클래스는 {@link Properties} 기반 설정 정보를 파싱하여, 로그 수집에 필요한
 * 세 가지 핵심 컴포넌트의 구현체를 지정하고 동적으로 인스턴스를 생성합니다:
 * <ul>
 *   <li>{@link LogCollectorPort} 수집기</li>
 *   <li>{@link LogValidatorPort} 유효성 검사기</li>
 *   <li>{@link LogPersistencePort} 로그 저장소</li>
 * </ul>
 *
 * <p>설정은 다음 키 값을 기반으로 구성됩니다:
 * <ul>
 *   <li>{@code worker.thread.count} - 로그 수집에 사용할 워커 스레드 개수</li>
 *   <li>{@code log.collector.type}, {@code log.validator.type}, {@code log.persistence.type} 등
 *       각 컴포넌트 유형 지정</li>
 * </ul>
 *
 * <p>클래스 내부적으로는 {@code Spec} 객체를 통해 각 구현체를 리플렉션 기반으로 생성합니다.
 *
 * @see LogCollectorPlugin
 * @see LogValidatorPlugin
 * @see LogPersistencePlugin
 */
public class LoggingRuntimeConfig {

  private final int threadCount;
  private final List<LogProcessorPlugin> logProcessorPlugins;
  private final long spendInitTime;

  /**
   * 내부 생성자. 주어진 {@link Properties} 설정으로부터 필수 설정 값을 읽고
   * 각 컴포넌트 스펙 객체를 초기화합니다.
   *
   * @param setting 로그 시스템 설정 프로퍼티
   * @throws IllegalArgumentException 설정 누락 또는 형식 오류 발생 시
   */
  // startTime, endTime 시간 재는데 간격때문에 발생하는 style경고 무시.
  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  private LoggingRuntimeConfig(Properties setting) {
    long startTime = System.nanoTime();

    String tempThreadCount = setting.getProperty("worker.thread.count");
    if (tempThreadCount == null || tempThreadCount.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'worker.thread.count' 가 존재하지 않습니다.");
    }
    try {
      threadCount = Integer.parseInt(tempThreadCount.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("'worker.thread.count'는 숫자여야 합니다: " + tempThreadCount);
    }

    // processorSpec 등록
    String tempProcessors = setting.getProperty("worker.processors");
    if (tempProcessors == null || tempProcessors.isBlank()) {
      throw new IllegalArgumentException("필수 설정 'worker.processors' 가 존재하지 않습니다.");
    }

    Map<String, String> settingMap = Map.copyOf(MapUtils.toMap(setting));
    List<LogProcessorPlugin> specs = new ArrayList<>();
    for (String processorName : tempProcessors.split(",")) {
      String key = processorName.trim();
      LogProcessorPluginPlanner processorSpecType = LogProcessorPluginPlanner.fromKey(key);
      LogProcessorPlugin logProcessorPlugin = processorSpecType.createSpecInstance(settingMap);
      specs.add(logProcessorPlugin);
    }
    if (specs.isEmpty()) {
      throw new IllegalStateException("no processors described");
    }
    this.logProcessorPlugins = List.copyOf(specs);

    this.spendInitTime = (System.nanoTime() - startTime) / 1_000_000L;
  }

  /**
   * 외부에서 {@link Properties} 기반 설정을 전달받아,
   * {@code LoggingRuntimeConfig} 인스턴스를 생성합니다.
   *
   * <p>이 메서드는 설정 유효성 검사를 포함하며, 실패 시 예외를 발생시킵니다.
   *
   * @param setting 설정 정보 (key-value 기반)
   * @return 유효한 {@code LoggingRuntimeConfig} 인스턴스
   * @throws IllegalArgumentException 필수 값이 누락되었거나 파싱 실패 시
   */
  public static LoggingRuntimeConfig from(Properties setting) {
    return new LoggingRuntimeConfig(setting);
  }

  /**
   * 로그 수집 작업에 사용할 워커 스레드 수를 반환합니다.
   *
   * @return 워커 스레드 개수
   */
  public int getWorkerThreadCount() {
    return this.threadCount;
  }

  /**
   * 로그 처리 파이프라인을 구성하기 위한 {@link LogProcessorPort} 인스턴스 목록을 생성합니다.
   *
   * <p>등록된 {@link LogProcessorPlugin} 목록을 기반으로 각 스펙에 정의된 프로세서 구현체를
   * 순차적으로 인스턴스화하여 파이프라인을 구성합니다.
   *
   * <p>생성된 프로세서들은 {@code LogWorker} 내에서 순차적으로 실행되며, 로그 메시지를
   * 수집, 검증, 저장 등의 단계별로 처리합니다.
   *
   * @return 파이프라인 구성에 사용될 {@link LogProcessorPort} 구현체 리스트
   *         (구현체는 {@link LogCollectorPort}, {@link LogValidatorPort}, {@link LogPersistencePort} 등을 포함할 수 있음)
   *
   * @see LogProcessorPort
   * @see LogProcessorPlugin#newProcessorInstance()
   * @see com.rihee.alerting.loggingService.core.runtime.LogWorker
   */
  public List<? extends LogProcessorPort> createProcessorChain() {
    return logProcessorPlugins.stream().map(LogProcessorPlugin::newProcessorInstance).toList();
  }

  /**
   * 현재 설정 상태를 문자열로 표현합니다.
   *
   * <p>디버깅 및 로그 용도로 사용되며, 각 컴포넌트의 FQCN을 함께 출력합니다.
   *
   * @return 구성 요약 문자열
   */
  @Override
  public String toString() {
    String processorDescriptions
        = logProcessorPlugins.stream()
                        .map(logProcessorSpec -> {
                          String className = logProcessorSpec.getClass().getSimpleName();
                          String processorType = logProcessorSpec.getProcessorType();
                          return className + "(" + processorType + ")";
                        })
                        .collect(Collectors.joining("->"));
    return String.format("LoggingRuntimeConfig{threadCount=%d, processors=%s, initTime=%s ms}",
                        threadCount,
                        processorDescriptions,
                        spendInitTime);
  }
}
