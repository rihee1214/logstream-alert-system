package com.rihee.alerting.mockservice.dao;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import org.springframework.stereotype.Component;

/**
 * {@code MockSearchDatabaseImpl}는 {@link MockSearchDatabase}의 mock 구현체로,
 * 테스트 환경 또는 로컬 개발 환경에서 간단한 조회 흐름을 검증하기 위한 구성 요소입니다.
 *
 * <p>실제 DB와 연동되지는 않으며, 고정된 문자열 반환 및 구조화 로그 출력을 통해
 * 시스템의 로깅 구조나 인터페이스 호출 흐름을 테스트할 수 있도록 설계되었습니다.
 * </p>
 *
 * <p>로그는 {@link StructuredLogger}를 통해 {@code BIZ} 타입으로 출력되며,
 * 전체 호출 흐름 상에서 MDC 기반 traceId, spanId 등을 포함한 로그 구조 검증을 보조합니다.
 * </p>
 *
 * <p><strong>⚠️ 예외 처리 관련 주의사항:</strong></p>
 * <ul>
 *   <li>현재 구현은 예외를 발생시키지 않도록 설계되어 있으나,</li>
 *   <li>향후 로직 확장 또는 외부 의존 추가 시 예외 가능성을 고려한 보호 로직이 필요합니다.</li>
 *   <li>구조화된 로그 출력 실패, null 반환, 미정의 분기 처리 등도 예외 발생 원인이 될 수 있습니다.</li>
 * </ul>
 *
 * @author 리희
 * @since 1.0
 */
@Component
public class MockSearchDatabaseImpl implements MockSearchDatabase {

  /**
   * 구조화된 비즈니스 로그 출력을 위한 {@link StructuredLogger} 인스턴스입니다.
   */
  private static final StructuredLogger logger
      = StructuredLoggerFactory.getLogger(MockSearchDatabase.class);

  /**
   * 단순 mock 데이터 조회 메서드로, 고정된 문자열 결과를 반환하고,
   * 결과값을 구조화 로그로 출력합니다.
   *
   * <p>해당 로그는 {@link LogType#BIZ} 타입으로 기록되며,
   * 로그 수집 및 구조 검증 목적의 테스트에 활용됩니다.
   *
   * @return {@code "simpleResult"} 라는 고정 문자열
   */
  @Override
  public String selectSimple() {
    logger.info(LogType.BIZ, "simple DB Select 처리 시작.");
    String result = "simpleResult";
    logger.info(LogType.BIZ, "simple select 결과 : {}", result);
    return result;
  }
}
