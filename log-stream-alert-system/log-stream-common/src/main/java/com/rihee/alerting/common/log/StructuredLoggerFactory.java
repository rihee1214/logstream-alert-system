package com.rihee.alerting.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 비즈니스(Biz) 및 시스템(Sys) 및 Actuator(act) 로그를 구분해서 기록할 수 있는
 * {@link StructuredLogger} 인스턴스를 생성하기 위한 팩토리 클래스입니다.
 *
 * <p>이 팩토리를 통해 생성된 {@code StructuredLogger}는 로그 타입에 따라
 * 자동으로 MDC 정보를 설정하고, 로그 호출 클래스를 명확히 남길 수 있도록 설계되었습니다.
 * </p>
 *
 * <h2>주요 목적</h2>
 * <ul>
 *     <li>Biz, Sys. Actuator 로그를 구분하여 분리 수집 및 모니터링 가능</li>
 *     <li>MDC 자동 관리로 일관된 로그 필드 유지</li>
 *     <li>SLF4J Logger를 wrapping하여 Class 정보 보존</li>
 * </ul>
 *
 * <p>StructuredLogger 사용 방법은 {@link StructuredLogger} 문서를 참고하세요.</p>
 *
 * <p><b>주의:</b> 이 팩토리 및 생성된 Logger는 비즈니스 로직을 처리하는 서비스 계층에서만 사용해야 합니다.
 * 공통 모듈이나 로그 수집 모듈 등에서는 사용을 지양하십시오.</p>
 *
 * @author 리희
 * @since 1.0
 */
public class StructuredLoggerFactory {

  // 생성자를 이용한 생성 금지
  private StructuredLoggerFactory() {
  }

  /**
   * 지정된 클래스 정보를 기준으로 {@link StructuredLogger} 인스턴스를 생성합니다.
   *
   * <p>생성된 Logger는 SLF4J Logger를 래핑하며, 로그 출력 시 호출 클래스의 정보를 보존합니다.</p>
   *
   * @param clazz 로그를 생성할 기준이 되는 클래스
   * @return {@link StructuredLogger} 인스턴스
   */
  public static StructuredLogger getLogger(Class<?> clazz) {
    Logger logger = LoggerFactory.getLogger(clazz);
    return new StructuredLoggerImpl(logger);
  }
}
