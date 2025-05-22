package com.rihee.alerting.mockservice.scheduler;

import java.util.concurrent.ScheduledFuture;

public class SchedulerRequest {

  private final String url;
  private final long intervalMs;
  private boolean shouldFail;
  private final ScheduledFuture<?> future;

  public SchedulerRequest(String url,
                        long intervalMs,
                        boolean shouldFail,
                        ScheduledFuture<?> future) {
    this.url = url;
    this.intervalMs = intervalMs;
    this.shouldFail = shouldFail;
    this.future = future;
  }

  public ScheduledFuture<?> getFuture() {
    return future;
  }

  public boolean shouldFail() {
    return shouldFail;
  }

  public void setShouldFail(boolean shouldFail) {
    this.shouldFail = shouldFail;
  }

  public String getUrl() {
    return url;
  }

  public long getIntervalMs() {
    return intervalMs;
  }
}

