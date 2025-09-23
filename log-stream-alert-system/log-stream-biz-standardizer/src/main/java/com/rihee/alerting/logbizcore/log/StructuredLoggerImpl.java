package com.rihee.alerting.logbizcore.log;

import static com.rihee.alerting.common.constant.logging.StructuredLogFields.LOG_TYPE;

import com.rihee.alerting.common.constant.logging.LogType;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;

/**
 * {@code StructuredLoggerImpl}는 {@link StructuredLogger}의 구현체로,
 * 시스템 로그와 비즈니스 로그를 structured하게 출력합니다.
 *
 * <p>이 클래스는 로그 호출 시 내부적으로 {@code MDC}를 활용하여
 * {@code LOG_TYPE}을 포함한 컨텍스트 정보를 자동으로 설정/복원합니다.
 * </p>
 *
 * <p>{@link LogType}
 * - BIZ 로그: 일반 정보성 로그<br>
 * - SYS 로그: 시스템 오류 및 예외성 로그 (Throwable 포함 가능)<br>
 * - ACT 로그: 시스템 metric, health 관련 로그
 * </p>
 *
 * <p>초기화 시 {@code MDC} 값을 함께 설정할 수 있으며, 이후 로그 호출마다 자동으로 처리됩니다.
 * </p>
 *
 * <p><b>⚠️ 사용 지침:</b><br>
 * 이 클래스는 <u>내부 구현체</u>입니다.<br>
 * <b>직접 사용하지 말고 반드시 {@link StructuredLoggerFactory}
 * 또는 {@link StructuredLogger} 인터페이스를 통해 접근하십시오.</b>
 * </p>
 *
 * <p><b>생성 방법:</b> {@code StructuredLoggerFactory.getLogger(MyClass.class)}를 사용할 것.</p>
 *
 * <p><b>📌 로그 레벨별 예외 출력 정책:</b><br>
 * - {@code error} 레벨에서만 {@code Throwable}을 포함한 로그 출력이 허용됩니다.<br>
 * - {@code warn}, {@code info}, {@code debug}에서는 stack trace 출력은 <b>허용되지 않으며</b>,
 *   필요 시 {@code e.getMessage()} 또는 커스텀 메시지를 명시적으로 출력해야 합니다.<br>
 * - 이는 로그 노이즈 방지 및 운영 환경에서의 로그 가독성 확보를 위한 정책입니다.
 * </p>
 */
public class StructuredLoggerImpl implements StructuredLogger {

  private final Logger log;

  /**
   * 생성자 - 외부에서 직접 호출하지 않고 StructuredLoggerFactory를 사용할 것.
   *
   * @param log SLF4J Logger
   */
  StructuredLoggerImpl(Logger log) {
    this.log = log;
  }

  /**
   * 추가적인 공통 MDC 값을 초기화할 때 사용한다.
   *
   * @param context 초기화할 MDC key-value Map
   */
  @Override
  public void initializeMdc(Map<String, String> context) {
    if (context != null) {
      context.forEach(MDC::put);
    }
  }

  /* ==============================  로그 처리 영역 ============================== */

  /**
   * 지정된 {@code logType}으로 DEBUG 레벨 로그를 기록합니다.
   */
  @Override
  public void debug(LogType logType, String message) {
    logWithMdc(logType, () -> log.debug(message));
  }

  /**
   * 지정된 {@code logType}으로 DEBUG 레벨 로그를 포맷 문자열과 인자를 이용해 기록합니다.
   */
  @Override
  public void debug(LogType logType, String message, Object... args) {
    logWithMdc(logType, () -> log.debug(message, args));
  }

  /**
   * 지정된 {@code logType}으로 INFO 레벨 로그를 기록합니다.
   */
  @Override
  public void info(LogType logType, String message) {
    logWithMdc(logType, () -> log.info(message));
  }

  /**
   * 지정된 {@code logType}으로 INFO 레벨 로그를 포맷 문자열과 인자를 이용해 기록합니다.
   */
  @Override
  public void info(LogType logType, String message, Object... args) {
    logWithMdc(logType, () -> log.info(message, args));
  }

  /**
   * 지정된 {@code logType}으로 WARN 레벨 로그를 기록합니다.
   */
  @Override
  public void warn(LogType logType, String message) {
    logWithMdc(logType, () -> log.warn(message));
  }

  /**
   * 지정된 {@code logType}으로 WARN 레벨 로그를 포맷 문자열과 인자를 이용해 기록합니다.
   */
  @Override
  public void warn(LogType logType, String message, Object... args) {
    logWithMdc(logType, () -> log.warn(message, args));
  }

  /**
   * 지정된 {@code logType}으로 ERROR 레벨 로그를 기록합니다.
   */
  @Override
  public void error(LogType logType, String message) {
    logWithMdc(logType, () -> log.error(message));
  }

  /**
   * 지정된 {@code logType}으로 ERROR 레벨 로그를 포맷 문자열과 인자를 이용해 기록합니다.
   */
  @Override
  public void error(LogType logType, String message, Object... args) {
    logWithMdc(logType, () -> log.error(message, args));
  }

  /**
   * 지정된 {@code logType}으로 ERROR 레벨 로그를 예외와 함께 기록합니다.
   */
  @Override
  public void error(LogType logType, String message, Throwable t) {
    logWithMdc(logType, () -> log.error(message, t));
  }

  /**
   * 지정된 {@code logType}으로 ERROR 레벨 로그를 예외 및 포맷 인자와 함께 기록합니다.
   */
  @Override
  public void error(LogType logType, String message, Throwable t, Object... args) {
    logWithMdc(logType, () -> log.error(message, args, t));
  }

  /* ============================== 내부 공통 처리 ============================== */

  /**
   * {@code LOG_TYPE}을 {@code MDC}에 설정한 뒤 로그를 실행하고, 이전 MDC 상태로 복원합니다.
   *
   * @param logType  {@link LogType} 참조
   * @param runnable 로그 실행 람다
   */
  private void logWithMdc(LogType logType, @NonNull Runnable runnable) {
    Map<String, String> contextSnapshot = MDC.getCopyOfContextMap();
    try {
      MDC.put(LOG_TYPE.getFieldName(), Objects.requireNonNullElse(logType, LogType.SYS).getCode());
      runnable.run();
    } finally {
      if (contextSnapshot != null) {
        MDC.setContextMap(contextSnapshot);
      } else {
        MDC.clear();
      }
    }
  }
}
