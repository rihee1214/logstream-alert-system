package com.rihee.alerting.logbizcore.log;

import com.rihee.alerting.common.constant.message.LogType;
import java.util.Map;

/**
 * {@code StructuredLogger}는 시스템 로그와 비즈니스 로그를 구분하여 structured logging을 지원하는 인터페이스입니다.
 *
 * <p>이 인터페이스는 로그 타입(sys/biz/act)을 명확히 구분하며,
 * 로그 레벨별로 메시지, 예외, 포맷 파라미터를 유연하게 처리할 수 있도록 다양한 오버로드 메서드를 제공합니다.
 * </p>
 *
 * <p>로그 호출 시 내부적으로 MDC를 활용하여 log_type, traceId, spanId, parentSpanId 등
 * 컨텍스트 정보를 자동으로 설정 및 정리합니다.
 * 이를 통해 분산 환경에서도 로그 추적성을 보장합니다.
 * </p>
 *
 * <p><b>사용 예시</b>: {@code StructuredLoggerFactory.getLogger(MyClass.class)} 를 통해 생성</p>
 *
 * <p><b>📌 로그 레벨별 예외 출력 정책:</b><br>
 * - {@code error} 레벨에서만 {@code Throwable}을 포함한 로그 출력이 허용됩니다.<br>
 * - {@code warn}, {@code info}, {@code debug}에서는 stack trace 출력은 <b>허용되지 않으며</b>,
 *   필요한 경우 {@code e.getMessage()} 또는 요약 메시지를 명시적으로 출력해야 합니다.<br>
 * - 이는 운영 환경에서의 로그 가독성 확보와 로그 노이즈 최소화를 위한 정책입니다.
 * </p>
 */

public interface StructuredLogger {

  /**
   * MDC에 공통 key-value context 정보를 초기화합니다.
   *
   * @param context MDC에 넣을 key-value 쌍
   */
  void initializeMdc(Map<String, String> context);

  // SYS 로그

  /**
   * 시스템 로그(DEBUG 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   */
  void debug(LogType logType, String message);

  /**
   * 시스템 로그(DEBUG 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   * @param args    포맷 인자
   */
  void debug(LogType logType, String message, Object... args);

  /**
   * 시스템 로그(INFO 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   */
  void info(LogType logType, String message);

  /**
   * 시스템 로그(INFO 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   * @param args    포맷 인자
   */
  void info(LogType logType, String message, Object... args);

  /**
   * 시스템 로그(WARN 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   */
  void warn(LogType logType, String message);

  /**
   * 시스템 로그(WARN 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   * @param args    포맷 인자
   */
  void warn(LogType logType, String message, Object... args);

  /**
   * 시스템 로그(ERROR 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   */
  void error(LogType logType, String message);

  /**
   * 시스템 로그(ERROR 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   * @param args    포맷 인자
   */
  void error(LogType logType, String message, Object... args);

  /**
   * 시스템 로그(ERROR 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   * @param t       예외 객체
   */
  void error(LogType logType, String message, Throwable t);

  /**
   * 시스템 로그(ERROR 레벨)를 기록합니다.
   *
   * @param logType 로그 타입
   * @param message 출력할 로그 메시지
   * @param t       예외 객체
   * @param args    포맷 인자
   */
  void error(LogType logType, String message, Throwable t, Object... args);
}
