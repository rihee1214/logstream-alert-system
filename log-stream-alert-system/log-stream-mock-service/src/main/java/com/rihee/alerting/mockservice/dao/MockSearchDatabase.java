package com.rihee.alerting.mockservice.dao;

/**
 * {@code MockSearchDatabase}는 mock-service의 테스트용 데이터 조회 인터페이스입니다.
 *
 * <p>이 인터페이스는 단순 쿼리 또는 데이터 접근 계층의 모킹(mocking)을 위해 설계되었으며,
 * 실제 DB 연결 없이 구조나 호출 흐름을 검증하는 데 목적을 둡니다.
 *
 * @author 리희
 * @since 1.0
 */
public interface MockSearchDatabase {

  /**
   * 단순 조회 쿼리를 수행하는 메서드입니다.
   *
   * <p>실제 DB 연동이 아닌 테스트용 Mock 구현 또는 구조 검증용으로 사용되며,
   * 간단한 문자열 결과를 반환합니다.
   *
   * @return 테스트용 단순 조회 결과 문자열
   */
  public String selectSimple();
}
