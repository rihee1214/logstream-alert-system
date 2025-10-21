package com.rihee.alerting.common.constant.storage;

import com.rihee.alerting.common.constant.logging.StructuredLogFields;

/**
 * 정상적으로 수집·검증을 통과한 로그를
 * “일반 로그 테이블”에 저장하기 위한 DB 스키마(컬럼) 열거형입니다.
 *
 * <p>정상 로그는 서비스 동작 상황을 추적하거나
 * 모니터링/분석을 위해 저장됩니다.
 * 하드코딩된 문자열 대신 본 enum의 상수를 사용하여
 * 컬럼명을 일관되고 안전하게 참조합니다.</p>
 *
 * @implNote {@link #getSchemaName()}가 반환하는 문자열은 실제 DB 컬럼명과 동일합니다.
 *           스키마가 변경되지 않는 한 수정하지 마세요.
 *
 * @see com.rihee.alerting.common.constant.storage.ErrorLogSchema
 */
public enum NormalLogSchema {
  /**
   * 로그의 유형을 나타내는 컬럼.
   * 예) biz, sys 등.
   */
  LOG_TYPE("log_type"),
  /**
   * 로그가 발생한 시점의 타임스탬프 컬럼.
   */
  TIME_STAMP("timestamp"),
  /**
   * 로그 레벨을 나타내는 컬럼.
   * 예) INFO, WARN, ERROR 등.
   */
  LOG_LEVEL("level"),
  /**
   * 로그를 발생시킨 서비스명을 기록하는 컬럼.
   */
  SERVICE_NAME("service_name"),
  /**
   * 로그를 남긴 클래스명(또는 모듈명)을 기록하는 컬럼.
   */
  CLASS_NAME("class_name"),
  /**
   * 로그 메시지 본문을 저장하는 컬럼.
   */
  MESSAGE("message"),
  /**
   * 로그가 발생한 호스트명을 기록하는 컬럼.
   */
  HOST("host"),
  /**
   * 로그가 발생한 컨테이너명을 기록하는 컬럼.
   */
  CONTAINER("container"),
  /**
   * 에러/예외 발생 시 스택트레이스를 저장하는 컬럼.
   */
  STACKTRACE("stacktrace"),
  /**
   * 분산 추적을 위한 Trace ID를 저장하는 컬럼.
   */
  TRACE_ID("trace_id"),
  /**
   * 분산 추적을 위한 Span ID를 저장하는 컬럼.
   */
  SPAN_ID("span_id"),
  /**
   * 부모 Span의 ID를 저장하는 컬럼.
   * 트랜잭션 계층 구조 파악에 사용됩니다.
   */
  PARENT_SPAN_ID("parent_span_id"),
  /**
   * 로그 포맷의 메이저 버전을 기록하는 컬럼.
   * 스키마/포맷 진화 시 호환성 판단에 사용됩니다.
   */
  LOG_VERSION_MAJOR("log_version_major"),
  /**
   * 추가적인 메타데이터(JSON 등)를 저장하는 컬럼.
   */
  META("meta");

  private final String schemaName;

  NormalLogSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  /**
   * 실제 DB에서 사용되는 컬럼명을 반환합니다.
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * 프로듀서(로그 발생지)에서 정의한 구조화 필드({@link StructuredLogFields})를
   * 스토리지 스키마({@link NormalLogSchema})로 1:1 매핑합니다.
   *
   * <p>발생지와 저장소의 명명 규칙이 다를 수 있으므로 중앙에서 매핑을 유지합니다.
   * 새 필드가 추가될 때 누락을 컴파일 타임에 바로 발견할 수 있도록 default 절을 두지 않습니다.</p>
   *
   * @param field 로그 발생지에서 사용한 구조화 필드(enum). {@code null}이면 예외가 발생합니다.
   * @return 매핑된 저장 스키마 열거값
   * @throws NullPointerException {@code field}가 {@code null}인 경우
   */
  public static NormalLogSchema fromStructuredLogField(StructuredLogFields field) {
    return switch (field) {
      case StructuredLogFields.LOG_TYPE -> LOG_TYPE;
      case StructuredLogFields.TIME_STAMP -> TIME_STAMP;
      case StructuredLogFields.LOG_LEVEL -> LOG_LEVEL;
      case StructuredLogFields.SERVICE_NAME -> SERVICE_NAME;
      case StructuredLogFields.CLASS_NAME -> CLASS_NAME;
      case StructuredLogFields.MESSAGE -> MESSAGE;
      case StructuredLogFields.HOST -> HOST;
      case StructuredLogFields.CONTAINER -> CONTAINER;
      case StructuredLogFields.STACKTRACE -> STACKTRACE;
      case StructuredLogFields.TRACE_ID -> TRACE_ID;
      case StructuredLogFields.SPAN_ID -> SPAN_ID;
      case StructuredLogFields.PARENT_SPAN_ID -> PARENT_SPAN_ID;
      default -> META;
    };
  }
}
