package com.rihee.alerting.common.constant.message;

/**
 * 로그 필드 키를 추상화하는 공통 인터페이스입니다.
 * 전체 스키마 설계 및 공통 정의에 대해서는
 * {@link com.rihee.alerting.common.constant.message package 설명}을 참고하세요.
 *
 * <p>이 인터페이스는 {@code LogMessage} 내부 구조에서 사용되는
 * 모든 로그 필드 명세(Enum 기반)를 통합하기 위한 기준 타입입니다.
 * </p>
 *
 * <p>{@code StructuredLogProperties}, {@code CallCommonProperties},
 * {@code HttpCallProperties} 등 다양한 로그 관련 Enum들이
 * 이 인터페이스를 구현하며, 이를 통해 로그 필드 키의 일관성,
 * 정형화된 접근, 검증 등을 지원합니다.
 * </p>
 *
 * <p>{@link #getFieldName()} 메서드는 실제 로그 메시지에서 사용되는
 * JSON key와 동일한 문자열을 반환합니다.
 * 이 필드는 저장소, 분석 도구, 로그 포맷팅 등에서
 * 직접 활용되는 문자열 키로 간주됩니다.
 * </p>
 *
 * <h3>예시:</h3>
 * <pre>{@code
 * public enum StructuredLogProperties implements LogFieldKey {
 *     LOGTYPE("logtype"),
 *     TIMESTAMP("timestamp");
 *
 *     private final String fieldName;
 *
 *     StructuredLogProperties(String fieldName) {
 *         this.fieldName = fieldName;
 *     }
 *
 *     public String getFieldName() {
 *         return fieldName;
 *     }
 * }
 * }</pre>
 *
 * @author 이리희
 *
 */
public interface LogFieldKey {

  /**
   * 로그 필드의 실제 이름을 반환합니다.
   *
   * <p>This value is used as the canonical field name in structured log messages,
   * and is expected to match the JSON key used in log output or persistence.</p>
   *
   * @implSpec
   *     구현체는 이 메서드가 반환하는 값이 실제 로그 출력 및 저장 시 사용되는
   *     키 이름과 정확히 일치하도록 보장해야 합니다.
   *     이 값은 고정된 계약 필드명으로 간주되며, 동적으로 변경되거나
   *     중복되면 안 됩니다.
   *
   * @return 로그 필드를 식별하는 문자열 키 (예: "traceId", "call.uri" 등)
   */
  String getFieldName();
}
