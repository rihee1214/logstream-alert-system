package com.rihee.alerting.loggingService.core.plan;

import com.rihee.alerting.loggingService.core.plugin.LogCollectorPlugin;
import com.rihee.alerting.loggingService.core.plugin.LogPersistencePlugin;
import com.rihee.alerting.loggingService.core.plugin.LogProcessorPlugin;
import com.rihee.alerting.loggingService.core.plugin.LogValidatorPlugin;
import java.util.Arrays;
import java.util.Map;

/**
 * 로그 파이프라인의 각 단계(Collect, Validate, Persist)에 대응하는
 * {@link LogProcessorPlugin} 생성 전략을 정의하는 enum입니다.
 *
 * <p>이 열거형은 주어진 단계 key("collect", "validate", "persist")를 기반으로
 * 적절한 {@link LogProcessorPlugin} 구현체를 생성하기 위한 팩토리 역할을 합니다.
 *
 * <p>주요 특징:
 * <ul>
 *   <li>{@link #COLLECTOR} : 로그 수집 단계 → {@link LogCollectorPlugin} 생성</li>
 *   <li>{@link #VALIDATOR} : 검증 단계 → {@link LogValidatorPlugin} 생성</li>
 *   <li>{@link #PERSISTENCE} : 영속화 단계 → {@link LogPersistencePlugin} 생성</li>
 * </ul>
 *
 * <p>사용 예시:
 * <pre>{@code
 * LogProcessorPluginPlanner planner = LogProcessorPluginPlanner.fromKey("collect");
 * LogProcessorPlugin plugin = planner.createSpecInstance(config);
 * }</pre>
 *
 * <p><b>예외 처리:</b><br>
 * {@link #fromKey(String)} 메서드는 잘못된 key가 주어지면
 * {@link IllegalArgumentException}을 발생시킵니다.
 *
 * @see LogProcessorPlugin
 * @see LogCollectorPlugin
 * @see LogValidatorPlugin
 * @see LogPersistencePlugin
 */
public enum LogProcessorPluginPlanner {

  /**
   * 로그 수집 단계(Collect)를 담당하는 플러그인 생성.
   * → {@link LogCollectorPlugin} 인스턴스를 생성합니다.
   */
  COLLECTOR("collector") {
    @Override
    public LogProcessorPlugin createSpecInstance(Map<String, String> setting) {
      return new LogCollectorPlugin(setting);
    }
  },
  /**
   * 로그 검증 단계(Validate)를 담당하는 플러그인 생성.
   * → {@link LogValidatorPlugin} 인스턴스를 생성합니다.
   */
  VALIDATOR("validator") {
    @Override
    public LogProcessorPlugin createSpecInstance(Map<String, String> setting) {
      return new LogValidatorPlugin(setting);
    }
  },
  /**
   * 로그 영속화 단계(Persist)를 담당하는 플러그인 생성.
   * → {@link LogPersistencePlugin} 인스턴스를 생성합니다.
   */
  PERSISTENCE("persistence") {
    @Override
    public LogProcessorPlugin createSpecInstance(Map<String, String> setting) {
      return new LogPersistencePlugin(setting);
    }
  };

  private final String key;

  LogProcessorPluginPlanner(String key) {
    this.key = key;
  }

  /**
   * 문자열 key를 기반으로 해당하는 {@code LogProcessorPluginPlanner}를 반환합니다.
   *
   * @param key 단계 구분 key ("collect", "validate", "persist")
   * @return 해당 단계에 대응하는 {@code LogProcessorPluginPlanner}
   * @throws IllegalArgumentException 알 수 없는 key가 주어진 경우
   */
  public static LogProcessorPluginPlanner fromKey(String key) {
    return Arrays.stream(values())
        .filter(e -> e.key.equalsIgnoreCase(key))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown processor type key: " + key));
  }

  /**
   * 설정 정보를 기반으로 해당 단계의 {@link LogProcessorPlugin} 인스턴스를 생성합니다.
   *
   * @param setting 플러그인 초기화를 위한 설정 값
   * @return 단계에 맞는 {@link LogProcessorPlugin} 인스턴스
   */
  public abstract LogProcessorPlugin createSpecInstance(Map<String, String> setting);
}
