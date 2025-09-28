package com.rihee.alerting.common.constant.storage;

/**
 * 메시지 수집/검증 단계에서 오류가 감지된 로그를
 * “에러 로그 테이블”에 저장하기 위한 DB 스키마(컬럼) 열거형입니다.
 *
 * <p>수집·검증 과정에서 실패한 메시지를 에러 레코드로 보존할 때,
 * 하드코딩된 문자열 대신 본 enum의 상수를 사용하여
 * 컬럼명을 일관되고 안전하게 참조합니다.</p>
 *
 * @implNote {@link #getSchemaName()}가 반환하는 문자열은 실제 DB 컬럼명과 동일합니다.
 *           스키마가 변경되지 않는 한 수정하지 마세요.
 *
 * @see com.rihee.alerting.common.constant.storage.NormalLogSchema
 */
public enum ErrorLogSchema {
  /**
   * 에러 로그 메시지의 고유 식별자 컬럼.
   * 예) 원본 메시지 키 또는 내부적으로 부여한 UUID 등.
   */
  MESSAGE_ID("message_id"),
  /**
   * 에러를 유발한 원본 로그 전문(raw text/json 등)을 보존하는 컬럼.
   * 원인 분석 및 재처리 시 참고합니다.
   */
  ORIGIN_LOG("origin_log"),
  /**
   * 에러 발생 사유(검증 실패 원인, 파싱 오류 상세 등)를 기록하는 컬럼.
   */
  REASON("reason"),
  /**
   * 에러가 감지·기록된 시점을 나타내는 타임스탬프 컬럼.
   */
  OCCURRED_AT("occurred_at"),
  /**
   * 에러가 발생한 처리 단계(수집/검증/저장 등)를 표시하는 컬럼.
   */
  STAGE("stage"),
  /**
   * 에러로 저장된 로그 포맷의 메이저 버전.
   * 스키마/포맷 진화 시 호환성 판단에 사용됩니다.
   */
  LOG_VERSION_MAJOR("log_version_major");

  private final String schemaName;

  ErrorLogSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  /**
   * 실제 DB에서 사용되는 컬럼명을 반환합니다.
   */
  public String getSchemaName() {
    return schemaName;
  }

}
