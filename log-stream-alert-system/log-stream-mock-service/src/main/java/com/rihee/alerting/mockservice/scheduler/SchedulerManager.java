package com.rihee.alerting.mockservice.scheduler;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * {@code SchedulerManager}는 외부에서 주기적인 HTTP 요청을 등록, 관리, 제거할 수 있도록 하는
 * mock 서비스 전용 스케줄러 관리 컴포넌트입니다.
 *
 * <p>등록된 요청은 내부적으로 {@link ScheduledExecutorService}를 통해 주기적으로 실행되며,
 * 요청 실패 여부(shouldFail)도 동적으로 제어할 수 있습니다.
 *
 * <p>스케줄러는 URL 단위로 관리되며, URL 중복 등록 시 이전 요청은 제거되고 새로 등록됩니다.
 *
 * @author 리희
 * @since 1.0
 */
@Component
public class SchedulerManager {

  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
  private final Map<String, SchedulerRequest> scheduledTasks = new ConcurrentHashMap<>();

  /**
   * 스케줄을 등록하거나 기존 요청을 갱신합니다.
   *
   * @param url        요청할 URL
   * @param intervalMs 주기 (밀리초)
   */
  public void register(String url, long intervalMs) {
    remove(url);

    Runnable task = () -> {
      SchedulerRequest req = scheduledTasks.get(url);
      if (req != null) {
        if (req.shouldFail()) {
          System.err.println("[Mock-Failure] Simulated failure for URL: " + url);
        } else {
          System.out.println("[Mock-Success] Requesting: " + url);
        }
      }
    };

    ScheduledFuture<?> future
        = executor.scheduleAtFixedRate(task, 0, intervalMs, TimeUnit.MILLISECONDS);
    scheduledTasks.put(url, new SchedulerRequest(url, intervalMs, false, future));
  }

  /**
   * 특정 URL에 대한 요청을 제거합니다.
   *
   * @param url 대상 URL
   */
  public void remove(String url) {
    SchedulerRequest existing = scheduledTasks.remove(url);
    if (existing != null) {
      existing.getFuture().cancel(true);
    }
  }

  /**
   * 특정 요청에 대해 실패 여부를 설정합니다.
   *
   * @param url       대상 URL
   * @param shouldFail true면 실패 상태로 설정
   */
  public void setFailure(String url, boolean shouldFail) {
    SchedulerRequest req = scheduledTasks.get(url);
    if (req != null) {
      req.setShouldFail(shouldFail);
    }
  }

  /**
   * 현재 등록된 URL 목록을 반환합니다.
   *
   * @return 등록된 URL Set
   */
  public Set<String> getRegisteredUrls() {
    return scheduledTasks.keySet();
  }

  /**
   * 애플리케이션 종료 시 스케줄러 종료.
   */
  @PreDestroy
  public void shutdown() {
    executor.shutdownNow();
  }
}
