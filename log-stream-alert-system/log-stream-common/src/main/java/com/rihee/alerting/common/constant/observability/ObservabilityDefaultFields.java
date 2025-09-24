package com.rihee.alerting.common.constant.observability;

public enum ObservabilityDefaultFields {

  /**
   * Zipkin 및 B3 헤더 호환용 trace sampling 여부.<br>
   * {@code 1} 또는 {@code 0} / {@code true} 또는 {@code false} 등의 값으로 표현됩니다.
   *
   * <p>B3 헤더: {@code X-B3-Sampled}
   */
  SAMPLED("sampled"),
  /**
   * Zipkin 및 B3 호환용 디버깅 수집 여부.<br>
   * 일반적으로 {@code 1} 또는 {@code 0}로 표현되며, 강제 수집 여부를 나타냅니다.
   *
   * <p>B3 헤더: {@code X-B3-Flags}
   */
  FLAGS("flags"),
  /**
   * 로그가 기록된 작업 또는 요청 처리의 소요 시간(ms 단위).
   *
   * <p>일반적으로 traceId 또는 spanId 단위의 duration을 측정하여 기록합니다.
   */
  DURATION("duration");

  private final String name;

  ObservabilityDefaultFields(String name) {
    this.name = name;
  }

  public String getFieldName() {
    return this.name;
  }
}
