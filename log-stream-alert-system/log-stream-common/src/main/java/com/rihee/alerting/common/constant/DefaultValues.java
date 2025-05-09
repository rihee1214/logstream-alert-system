package com.rihee.alerting.common.constant;

/**
 * 공용으로 사용되는 환경 변수 및 설정 값의 기본값을 정의하는 열거형입니다.
 *
 * <p>애플리케이션 환경 변수 또는 시스템 프로퍼티가 정의되지 않았거나 누락된 경우,
 * 기본적으로 사용될 값을 제공하기 위해 설계되었습니다.</p>
 *
 * <p>예: 서비스 이름, 컨테이너 ID, 호스트 이름 등이 설정되지 않은 경우,
 * 해당 로그 필드에는 이 enum({@code LOGGING_DEFAULT_VALUE}) 에서 정의한 기본값이 사용됩니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
public enum DefaultValues {
  /**
   * 로그 필드의 기본값으로 사용되는 상수입니다.
   *
   * <p>예: 서비스 이름, 컨테이너 ID, 호스트 이름 등이 설정되지 않은 경우,
   * 해당 로그 필드에는 해당 열거형 상수 값에 정의된 값이 사용됩니다.</p>
   */
  LOGGING_DEFAULT_VALUE("__UNDEFINED__"),
  /**
   * Prometheus 인증 헤더 값이 설정되지 않았을 경우 사용되는 기본값입니다.
   *
   * <p>Prometheus가 actuator endpoint를 호출할 때 전달하는 {@code X-Monitoring-Token}
   * 헤더가 누락되었거나 환경 변수로 주입되지 않았을 경우, 이 기본값이 대체됩니다.</p>
   *
   * <p>기본값은 {@code "__UNDEFINED__"}로 지정되며, 이는 명시적 설정 누락 시 로그 또는 시스템 내에서
   * "정의되지 않은 상태"를 나타내는 표준 문자열로 사용됩니다.</p>
   */
  PROMETHEUS_TOKEN_DEFAULT("__UNDEFINED__");

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
