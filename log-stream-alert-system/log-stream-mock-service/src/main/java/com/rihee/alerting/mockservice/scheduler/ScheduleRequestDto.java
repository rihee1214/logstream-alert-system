package com.rihee.alerting.mockservice.scheduler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * {@code ScheduleRequestDto}는 mock scheduler에 동적으로 요청을 등록하기 위한 데이터 전달 객체입니다.
 *
 * <p>요청 대상 URL, 요청 주기, 실패 응답 시뮬레이션 여부를 포함합니다.
 *
 * @author 리희
 * @since 1.0
 */
public class ScheduleRequestDto {

  @NotBlank(message = "url은 필수 입력값입니다.")
  private String url;

  @Positive(message = "intervalMs는 0보다 커야 합니다.")
  private long intervalMs = 10000; // 기본 10초

  private boolean shouldFail = false;

  public ScheduleRequestDto() {
  }

  public ScheduleRequestDto(String url, long intervalMs, boolean shouldFail) {
    this.url = url;
    this.intervalMs = intervalMs;
    this.shouldFail = shouldFail;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public long getIntervalMs() {
    return intervalMs;
  }

  public void setIntervalMs(long intervalMs) {
    this.intervalMs = intervalMs;
  }

  public boolean isShouldFail() {
    return shouldFail;
  }

  public void setShouldFail(boolean shouldFail) {
    this.shouldFail = shouldFail;
  }
}
