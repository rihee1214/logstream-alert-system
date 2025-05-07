package com.rihee.alerting.common.log.enums;

/**
 * 구조화(Structured) 로깅 시 사용되는 표준 필드 정의 Enum.<br>
 * 모든 로그는 이 Enum에서 정의한 표준 필드명을 사용하여 생성된다.<br>
 * 시스템 간 로그 표준화 및 유지보수를 위해 통일된 키 네이밍 규칙을 따른다.
 */
public enum StructuredLogProperties {

  /**
   * 로그 타입.<br>
   * 업무 : biz, 시스템 : sys, 기타 : default
   */
  LOG_TYPE("logtype"),
  /**
   * 로그 발생 시간.<br>
   * 포맷: ISO 8601 DateTime + Offset (예: 2025-04-27T15:16:15.641+0900)
   */
  TIME_STAMP("timestamp"),
  /**
   * 로그 레벨.<br>
   * DEBUG, INFO, WARN, ERROR 중 하나의 값을 가진다.
   */
  LEVEL("level"),
  /**
   * 서비스 이름.<br>
   * 해당 로그를 발생시킨 애플리케이션 또는 마이크로서비스의 논리적 이름을 의미한다.
   */
  SERVICE("service"),
  /**
   * 트레이스 ID.<br>
   * 분산 트랜잭션 추적을 위한 전체 요청 흐름 식별자.
   */
  TRACE_ID("traceId"),
  /**
   * 스팬 ID.<br>
   * 트레이스 ID 안에서의 개별 작업(스팬)을 식별하는 ID.
   */
  SPAN_ID("spanId"),
  /**
   * 부모 스팬 ID.<br>
   * 요청 흐름의 트리 구조를 구성하고, 호출 관계를 추적하기 위해 사용.
   */
  PARENT_SPAN_ID("parentSpanId"),
  /**
   * 로그 메시지 본문.<br>
   * 로깅 대상이 되는 핵심 메세지 문자열.
   */
  MESSAGE("message"),
  /**
   * 로그를 기록한 클래스 이름.<br>
   * FQCN(Fully Qualified Class Name) 형태로 기록된다.
   */
  CLASS("class"),
  /**
   * 로그를 기록한 서버의 호스트명.<br>
   * 물리/가상 머신 단위로 고유하게 식별할 수 있는 이름.
   */
  HOST("host"),
  /**
   * 로그를 기록한 컨테이너 이름.<br>
   * Docker/Kubernetes 등 컨테이너 기반 환경에서의 인스턴스 구분용.
   */
  CONTAINER("container"),
  /**
   * 추가 메타데이터를 담는 영역.<br>
   * 특정 API 호출이나 비즈니스 흐름에서 동적으로 추가할 수 있는 키-값 쌍.
   */
  STACK_TRACE("stacktrace"),
  /**
   * 추가 메타데이터를 담는 영역.<br>
   * 특정 API 호출이나 비즈니스 흐름에서 동적으로 추가할 수 있는 키-값 쌍.
   */
  META("meta");


  private final String name;

  StructuredLogProperties(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
