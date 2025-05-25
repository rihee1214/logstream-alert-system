package com.rihee.alerting.common.constant;

/**
 * 공용으로 사용되는 환경 변수 및 설정 값의 기본값을 정의하는 열거형입니다.
 *
 * <p>애플리케이션 환경 변수 또는 시스템 프로퍼티가 정의되지 않았거나 누락된 경우,
 * 기본적으로 사용될 값을 제공하기 위해 설계되었습니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
public enum DefaultValues {
  /**
   * B3 헤더의 {@code X-B3-Sampled} 필드가 누락된 경우 사용되는 기본값입니다.
   *
   * <p>기본값은 {@code "0"}이며, 추적 비활성화를 의미합니다.
   * 자세한 내용은 {@link com.rihee.alerting.common.constant.B3Header}를 참고하세요.</p>
   */
  B3HEADER_SAMPLED_DEFAULT("0"),
  /**
   * 서비스와 같이 알림의 대상이어야 하지만, 그 정보를 찾을 수 없는 경우 사용하는 값.
   */
  UNKNOWN("__UNKNOWN__");

  private final String value;

  DefaultValues(String value) {
    this.value = value;
  }

  /**
   * 열거형 상수에 연결된 실제 문자열 값을 반환합니다.
   *
   * @return 기본값 문자열 (예: {@code "__UNDEFINED__"})
   */
  public String getValue() {
    return this.value;
  }
}
