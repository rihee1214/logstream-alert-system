package com.rihee.alerting.common.constant;

/**
 * B3 Propagation 기반의 HTTP 헤더 키를 정의한 열거형입니다.
 *
 * <p>B3는 분산 추적(Distributed Tracing) 시스템에서 사용하는 표준 헤더 규약이며,
 * 이 열거형은 요청 간 트레이싱 정보를 전파하기 위해 사용되는 모든 B3 관련 헤더 키를 정의합니다.</p>
 *
 * <p>해당 헤더는 주로 {@code traceId}, {@code spanId}, {@code parentSpanId}, {@code sampled},
 * {@code flags} 등을 포함하며, 마이크로서비스 간 요청 추적 및 로그 상관관계를 위해 활용됩니다.</p>
 *
 * @author 리희
 * @since 1.0
 * @see <a href="https://github.com/openzipkin/b3-propagation#multiple-headers">B3 Propagation 공식 문서 (GitHub)</a>
 */
public enum B3Header {
  /**
   * 전체 요청 흐름을 식별하는 고유 ID를 나타내는 헤더입니다.
   *
   * <p>모든 서비스 호출에서 동일한 {@code traceId}를 유지하며,
   * 이를 통해 하나의 요청 흐름으로 묶인 모든 로그를 추적할 수 있습니다.</p>
   */
  TRACE_ID("X-B3-TraceId"),
  /**
   * 현재 서비스 또는 작업 단위를 식별하는 고유 ID를 나타내는 헤더입니다.
   *
   * <p>{@code spanId}는 해당 요청에서 수행 중인 작업 단위이며,
   * 로그 상에서 세분화된 요청 추적이 가능하도록 지원합니다.</p>
   */
  SPAN_ID("X-B3-SpanId"),
  /**
   * 현재 요청이 파생된 상위 작업(span)을 식별하는 ID를 나타냅니다.
   *
   * <p>서비스 간 호출에서 상위 {@code spanId}가 존재할 경우, 이 값을 통해 계층적인 요청 구조를 구성할 수 있습니다.</p>
   */
  PARENT_SPAN_ID("X-B3-ParentSpanId"),
  /**
   * 해당 요청이 추적 대상인지 여부를 나타내는 플래그입니다.
   *
   * <p>{@code "1"}은 추적 대상으로 설정되어 Trace 시스템(ZIPKIN 등)에 전송됨을 의미하며,
   * {@code "0"}은 비추적 대상임을 의미합니다.
   * 요청 헤더에 해당 값이 존재하지 않으면 기본값은 {@code "0"}(비추적)으로 간주됩니다.</p>
   *
   * <p>해당 값은 로깅 수집 여부에는 영향을 주지 않으며, 분산 추적 시스템에서 수집 여부 판단에 활용됩니다.</p>
   */
  SAMPLED("X-B3-Sampled"),
  /**
   * 디버깅이나 강제 추적 등을 위한 추가적인 플래그입니다.
   *
   * <p>예: {@code 1} 값은 강제 추적(디버깅 목적으로)을 의미할 수 있습니다.
   * 일반적으로 {@code sampled}와 함께 사용됩니다.</p>
   */
  FLAGS("X-B3-Flags");

  private final String headerName;

  B3Header(String headerName) {
    this.headerName = headerName;
  }

  /**
   * 열거형 상수에 해당하는 HTTP 헤더 키 문자열을 반환합니다.
   *
   * @return B3 헤더 키 (예: {@code "X-B3-TraceId"})
   */
  public String getHeaderName() {
    return headerName;
  }
}
