package com.rihee.alerting.loggingService.core;

import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.collectors.LogCollectorSpec;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.rihee.alerting.loggingService.persistence.LogPersistenceSpec;
import com.rihee.alerting.loggingService.validators.LogValidator;
import com.rihee.alerting.loggingService.validators.LogValidatorSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * {@code LoggingRuntimeConfig}는 로그 수집 시스템의 런타임 구성 정보를 캡슐화한 설정 객체입니다.
 *
 * <p>이 클래스는 {@link Properties} 기반 설정 정보를 파싱하여, 로그 수집에 필요한
 * 세 가지 핵심 컴포넌트의 구현체를 지정하고 동적으로 인스턴스를 생성합니다:
 * <ul>
 *   <li>{@link LogCollector} 수집기</li>
 *   <li>{@link LogValidator} 유효성 검사기</li>
 *   <li>{@link LogPersistence} 로그 저장소</li>
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
 * @see LogCollectorSpec
 * @see LogValidatorSpec
 * @see LogPersistenceSpec
 */
public class LoggingRuntimeConfig {

  private final int threadCount;
  private final List<LogProcessorSpec> logProcessorSpecs = new ArrayList<>();

  /**
   * 내부 생성자. 주어진 {@link Properties} 설정으로부터 필수 설정 값을 읽고
   * 각 컴포넌트 스펙 객체를 초기화합니다.
   *
   * @param setting 로그 시스템 설정 프로퍼티
   * @throws IllegalArgumentException 설정 누락 또는 형식 오류 발생 시
   */
  private LoggingRuntimeConfig(Properties setting) {
    String tempThreadCount = setting.getProperty("worker.thread.count");
    if (StringUtils.isEmpty(tempThreadCount)) {
      throw new IllegalArgumentException("필수 설정 'worker.thread.count' 가 존재하지 않습니다.");
    }
    try {
      threadCount = Integer.parseInt(tempThreadCount);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("'worker.thread.count'는 숫자여야 합니다: " + tempThreadCount);
    }

    // processorSpec 등록
    String tempProcessors = setting.getProperty("worker.processors");
    if (StringUtils.isEmpty(tempProcessors)) {
      throw new IllegalArgumentException("필수 설정 'worker.processors' 가 존재하지 않습니다.");
    }
    String[] processors = tempProcessors.split(",");
    for (String processorName : processors) {
      LogProcessorSpecType processorSpecType = LogProcessorSpecType.fromKey(processorName);
      Map<String, String> settingMap = MapUtils.toMap(setting);
      LogProcessorSpec logProcessorSpec = processorSpecType.createSpecInstance(settingMap);
      this.logProcessorSpecs.add(logProcessorSpec);
    }
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


  public List<? extends LogProcessor> createProcessorChain() {
    return logProcessorSpecs.stream().map(LogProcessorSpec::newProcessorInstance).toList();
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
        = logProcessorSpecs.stream()
                        .map(logProcessorSpec -> {
                          String className = logProcessorSpec.getClass().getSimpleName();
                          String processorType = logProcessorSpec.getProcessorType();
                          return className + "(" + processorType + ")";
                        })
                        .reduce((a, b) -> a + "->" + b)
                        .orElse("no processors described");
    return String.format("LoggingRuntimeConfig{threadCount=%d, processors=%s}",
                        threadCount,
                        processorDescriptions);
  }
}
