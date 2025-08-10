package com.rihee.alerting.common.util;

/**
 * 문자열 관련 유틸리티 메서드를 제공하는 클래스입니다.
 *
 * <p>이 클래스는 인스턴스화할 수 없습니다. 모든 메서드는 정적(static)으로 제공됩니다.
 */
public final class StringUtils {

  /**
   * 기본 생성자를 비공개로 설정하여 인스턴스 생성을 방지합니다.
   */
  private StringUtils() {
  }

  /**
   * 주어진 문자열이 비어 있지 않은지 검사합니다.
   *
   * <p>문자열이 {@code null}이 아니고, 공백이 아닌 문자를 하나 이상 포함하면
   * {@code true}를 반환합니다. 즉, 의미 있는 값이 포함된 경우를 판별합니다.</p>
   *
   * @param validateTarget 검사할 문자열
   * @return 유효한 문자열이면 {@code true}, {@code null}이거나 공백 문자열이면 {@code false}
   */
  public static boolean isNotBlank(String validateTarget) {
    return validateTarget != null && !validateTarget.isBlank();
  }

  /**
   * 주어진 문자열이 비어 있는지 검사합니다.
   *
   * <p>문자열이 {@code null}이거나, 길이가 0이거나,
   * 공백 문자만으로 구성된 경우 {@code true}를 반환합니다.
   * 즉, 의미 있는 값이 없는 경우를 판별합니다.</p>
   *
   * @param validateTarget 검사할 문자열
   * @return 비어 있거나 공백 문자열이면 {@code true}, 그렇지 않으면 {@code false}
   */
  public static boolean isBlank(String validateTarget) {
    return validateTarget == null || validateTarget.isBlank();
  }

  /**
   * 주어진 문자열이 {@code null}이거나 공백(whitespace)만 포함할 경우
   * {@link IllegalArgumentException}을 발생시킵니다.
   *
   * <p>이 메서드는 호출자가 제공한 문자열이 비어 있지 않음을 보장하기 위해 사용됩니다.
   * 필수 입력값 검증에 적합하며, 검증에 실패하면 {@code IllegalArgumentException}이 발생합니다.
   *
   * @param value 검증할 문자열
   * @return 공백이 아닌 원본 문자열
   * @throws IllegalArgumentException 주어진 값이 {@code null}이거나 공백 문자열인 경우
   */
  public static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank or null");
    }
    return value;
  }
}
