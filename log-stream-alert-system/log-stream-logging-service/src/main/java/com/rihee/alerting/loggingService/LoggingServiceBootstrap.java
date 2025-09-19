package com.rihee.alerting.loggingService;

import com.rihee.alerting.loggingService.core.runtime.LogWorker;
import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code LoggingServiceBootstrap}는 로깅 서비스 애플리케이션의
 * 실행 진입점으로, 런타임 설정을 로드하고 {@link LogWorker} 스레드를 시작하는 역할을 합니다.
 *
 * <p>이 클래스는 서비스 구동 시 필수적인 초기화 로직을 담당하며,
 * 설정 로드, 워커 스레드 풀 생성, 워커 실행까지의 과정을 총괄합니다.
 *
 * @see LogWorker
 * @see LoggingRuntimeConfig
 * @see SettingLoader
 */
public class LoggingServiceBootstrap {

  /**
   * 애플리케이션 실행 시 호출되는 진입점 메서드입니다.
   *
   * <p>동작 순서:
   * <ol>
   *   <li>{@link SettingLoader}를 통해 클래스패스에서 {@link LoggingRuntimeConfig}를 로드</li>
   *   <li>설정에 지정된 워커 스레드 개수를 조회</li>
   *   <li>{@link ExecutorService}를 생성하고 해당 개수만큼 {@link LogWorker} 인스턴스를 실행</li>
   *   <li>try-with-resources 구문으로 스레드 풀을 안전하게 종료</li>
   * </ol>
   *
   * <p><strong>주의:</strong> 워커 스레드 개수는 서비스 처리량과 리소스 사용량에 직접적인 영향을 주므로
   * 환경에 맞게 적절히 조정해야 합니다.
   *
   * @param args 실행 인자(현재 사용되지 않음)
   */
  public static void main(String[] args) {

    LoggingRuntimeConfig config = SettingLoader.loadRuntimeSettingFromClasspath();

    int threadCount = config.getWorkerThreadCount();
    try (ExecutorService service = Executors.newFixedThreadPool(threadCount);) {
      for (int i = 0; i < threadCount; i++) {
        service.execute(new LogWorker(config.createProcessorChain()));
      }
    }
  }

}
