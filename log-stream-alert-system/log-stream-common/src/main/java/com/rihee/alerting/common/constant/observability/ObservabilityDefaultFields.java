package com.rihee.alerting.common.constant.observability;

/**
 * 관측가능성(Observability) 기본 필드 키를 표준화하여 제공하는 열거형입니다.
 *
 * <p>로그/트레이싱/메트릭 파이프라인 전반에서 반복적으로 사용되는 핵심 필드를
 * 하드코딩 문자열 대신 상수로 제공함으로써, 오타를 방지하고 스키마(키 이름)의
 * 일관성을 유지합니다. 특히 Zipkin/B3 헤더 체계와의 호환을 고려하여 필드 명을
 * 정의했습니다.</p>
 *
 * <h2>용도</h2>
 * <ul>
 *   <li>로그 메타 영역(meta JSON) 또는 구조화 로그의 공통 키로 사용</li>
 *   <li>HTTP 헤더/컨텍스트 전파 시 표준 키로 사용</li>
 *   <li>트레이스 수집기/밸리데이터/퍼시스턴스 어댑터 간 계약(Contract) 고정</li>
 * </ul>
 *
 * <h2>키 명명</h2>
 * <p>{@link #getFieldName()}는 시스템 전반에서 사용하는 정규화된(소문자 등)
 * 실제 키 문자열을 반환합니다. 파이프라인 간 호환성을 위해 임의 변경을
 * 지양하세요.</p>
 *
 * @implNote B3 호환 필드(SAMPLED, FLAGS)는 Zipkin/B3 헤더 스펙과 의미를 맞추되,
 *           내부 저장 시 소문자 키를 사용합니다. 퍼시스턴스/인덱싱 시스템과의
 *           충돌을 피하려면 가능한 한 본 열거형의 값을 그대로 사용하십시오.
 *
 * @see <a href="https://github.com/openzipkin/b3-propagation">B3 Propagation</a>
 * @see <a href="https://zipkin.io/">Zipkin</a>
 */
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
