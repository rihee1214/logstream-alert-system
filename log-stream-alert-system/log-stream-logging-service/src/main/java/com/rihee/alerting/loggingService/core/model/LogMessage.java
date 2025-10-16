package com.rihee.alerting.loggingService.core.model;

import com.rihee.alerting.common.identity.LogMessageKeyGenerator;

/**
 * 로그 메시지를 표현하는 핵심 <b>추상 기반 클래스</b>입니다.
 *
 * <p>정상적으로 수집·검증된 로그
 * ({@link com.rihee.alerting.loggingService.core.model.LogNormalMessage})와
 * 수집 실패나 검증 실패 등 오류 상황을 기록하기 위한 로그
 * ({@link com.rihee.alerting.loggingService.core.model.LogErrorMessage})를
 * 공통으로 다루기 위해 정의되었습니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>로그 메시지를 식별할 수 있는 고유 key 제공 ({@link #getMessageKey()})</li>
 *   <li>정상 로그/에러 로그 여부 확인 ({@link #isError()})</li>
 *   <li>로그 데이터의 개별 속성 접근 및 수정 ({@link #get(String)}, {@link #put(String, Object)})</li>
 *   <li>로그 전체를 JSON 문자열로 직렬화 ({@link #toJsonString()})</li>
 * </ul>
 *
 * <p>이 클래스를 상속하는 구현체는 로그 수집 파이프라인 전반에서
 * 공통적으로 다뤄질 수 있도록 설계되어야 합니다.
 */
public abstract sealed class LogMessage permits LogNormalMessage, LogErrorMessage {

  /**
   * 로그 메시지를 식별하기 위한 고유 key를 반환합니다.
   *
   * @return 로그 메시지 key
   */
  public abstract String getMessageKey();

  /**
   * 이 로그가 에러 로그인지 여부를 반환합니다.
   *
   * @return 에러 로그라면 {@code true}, 정상 로그라면 {@code false}
   */
  public abstract boolean isError();

  /**
   * 주어진 key에 해당하는 로그 속성을 반환합니다.
   *
   * @param key 조회할 속성의 key
   * @return 해당 key에 매핑된 값, 존재하지 않으면 {@code null}
   */
  public abstract Object get(String key);

  /**
   * 주어진 key와 value를 로그 속성에 추가하거나 갱신합니다.
   *
   * @param key 속성 key
   * @param value 속성 값 (null 허용 여부는 구현체 정책에 따름)
   */
  public abstract void put(String key, Object value);

  /**
   * 로그 전체를 JSON 문자열로 직렬화합니다.
   *
   * <p>이 메서드는 주로 다른 메시지 타입으로 변환하거나,
   * 외부로 전달하기 위한 직렬화 과정에서 사용됩니다.
   *
   * @return 로그의 JSON 표현 (null이 아님)
   */
  public abstract String toJsonString();

  /**
   * 고유한 로그 메시지 key를 생성합니다.
   *
   * <p>기본 구현은 {@link LogMessageKeyGenerator}를 위임 호출하여
   * 충돌 가능성이 매우 낮은 식별자를 생성합니다. 구현체에서 메시지 생성 시
   * 공통적으로 사용할 수 있도록 {@code protected} 정적 헬퍼로 제공합니다.
   *
   * @return 새로 생성된 메시지 key (null이 아님)
   */
  protected static String generateKey() {
    return LogMessageKeyGenerator.generate();
  }
}
