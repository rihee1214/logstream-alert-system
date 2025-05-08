package com.rihee.alerting.common.log.constant;

/**
 * {@code LogType}은 로그의 유형을 구분하기 위한 열거형입니다.
 *
 * <p>각 로그 타입은 시스템, 비즈니스, 액추에이터 로그로 구분되며,
 * 로그 백엔드(Appender) 또는 로그 분석 도구에서의 분류 필드로 활용됩니다.
 * </p>
 *
 * <p>해당 enum은 MDC 설정, 로깅 출력, 라우팅 처리 등 다양한 로깅 관련 기능에서
 * 일관된 로그 타입 구분자로 사용됩니다.
 * </p>
 */
public enum LogType {
  /**
   * 비즈니스 로직과 관련된 일반 정보성 로그.
   *
   * <p>사용자의 행동, 처리 흐름, 비즈니스 상태 등 애플리케이션의 주요 기능 동작을 설명하는 로그입니다.
   * </p>
   */
  BIZ("biz"),
  /**
   * 액추에이터(Actuator) 관련 로그.
   *
   * <p>헬스 체크, 메트릭, 트래픽 모니터링 등의 운영/관제 목적의 로그를 표현합니다.
   * </p>
   */
  ACT("act"),
  /**
   * 시스템 또는 인프라 관련 로그.
   *
   * <p>예외, 에러, 경고, 시스템 이벤트 등 내부 상태 및 장애 상황을 나타내는 로그입니다.
   * </p>
   */
  SYS("sys");

  private final String code;

  /**
   * 로그 타입 생성자.
   *
   * @param code 외부 출력 또는 매핑을 위한 문자열 코드
   */
  LogType(String code) {
    this.code = code;
  }

  /**
   * 해당 로그 타입에 연결된 문자열 코드를 반환합니다.
   *
   * @return 로그 타입 코드 (예: "biz", "sys", "act")
   */
  public String getCode() {
    return code;
  }

  /**
   * 주어진 코드에 해당하는 LogType을 반환합니다.
   *
   * @param code 문자열 코드
   * @return 해당하는 LogType
   * @throws IllegalArgumentException 알 수 없는 코드일 경우
   */
  public static LogType fromCode(String code) {
    for (LogType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown LogType code: " + code);
  }
}
