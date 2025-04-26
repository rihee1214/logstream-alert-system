package com.rihee.alerting.common.log;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 로그를 structured하게 찍기 위해 wrapping한 Logger.
 *
 * <p>
 * - BIZ 로그: 일반 정보성 로그
 * - SYS 로그: 시스템 에러/예외성 로그 (Throwable 포함 가능)
 * <p>
 * 초기화 시 MDC 값을 함께 설정할 수 있으며, 이후 자동으로 처리된다.
 */
public class StructuredLogger {

    private static final String LOG_TYPE_KEY = "logtype";
    private static final String SYS_LOG_TYPE = "sys";
    private static final String BIZ_LOG_TYPE = "biz";

    private final Logger log;

    /**
     * 생성자 - 외부에서 직접 호출하지 않고 StructuredLoggerFactory를 사용할 것.
     * @param log SLF4J Logger
     */
    StructuredLogger(Logger log) {
        this.log = log;
    }

    /**
     * 추가적인 공통 MDC 값을 초기화할 때 사용한다.
     *
     * @param context 초기화할 MDC key-value Map
     */
    public void initializeMdc(Map<String, String> context) {
        if (context != null) {
            context.forEach(MDC::put);
        }
    }

    /* ============================== SYS 로그 영역 ============================== */

    /** 시스템(debug) 로그를 남긴다. */
    public void debugSys(String message) {
        logWithMdc(SYS_LOG_TYPE, () -> log.debug(message));
    }

    /** 시스템(debug) 로그를 포맷 파라미터와 함께 남긴다. */
    public void debugSys(String message, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.debug(message, args));
    }

    /** 시스템(debug) 로그를 예외와 함께 남긴다. */
    public void debugSys(String message, Throwable t) {
        logWithMdc(SYS_LOG_TYPE, () -> log.debug(message, t));
    }

    /** 시스템(debug) 로그를 예외와 포맷 파라미터 모두와 함께 남긴다. */
    public void debugSys(String message, Throwable t, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.debug(message, args, t));
    }

    /** 시스템(info) 로그를 남긴다. */
    public void infoSys(String message) {
        logWithMdc(SYS_LOG_TYPE, () -> log.info(message));
    }

    /** 시스템(info) 로그를 포맷 파라미터와 함께 남긴다. */
    public void infoSys(String message, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.info(message, args));
    }

    /** 시스템(info) 로그를 예외와 함께 남긴다. */
    public void infoSys(String message, Throwable t) {
        logWithMdc(SYS_LOG_TYPE, () -> log.info(message, t));
    }

    /** 시스템(info) 로그를 예외와 포맷 파라미터 모두와 함께 남긴다. */
    public void infoSys(String message, Throwable t, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.info(message, args, t));
    }

    /** 시스템(warn) 로그를 남긴다. */
    public void warnSys(String message) {
        logWithMdc(SYS_LOG_TYPE, () -> log.warn(message));
    }

    /** 시스템(warn) 로그를 포맷 파라미터와 함께 남긴다. */
    public void warnSys(String message, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.warn(message, args));
    }

    /** 시스템(warn) 로그를 예외와 함께 남긴다. */
    public void warnSys(String message, Throwable t) {
        logWithMdc(SYS_LOG_TYPE, () -> log.warn(message, t));
    }

    /** 시스템(warn) 로그를 예외와 포맷 파라미터 모두와 함께 남긴다. */
    public void warnSys(String message, Throwable t, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.warn(message, args, t));
    }

    /** 시스템(error) 로그를 남긴다. */
    public void errorSys(String message) {
        logWithMdc(SYS_LOG_TYPE, () -> log.error(message));
    }

    /** 시스템(error) 로그를 포맷 파라미터와 함께 남긴다. */
    public void errorSys(String message, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.error(message, args));
    }

    /** 시스템(error) 로그를 예외와 함께 남긴다. */
    public void errorSys(String message, Throwable t) {
        logWithMdc(SYS_LOG_TYPE, () -> log.error(message, t));
    }

    /** 시스템(error) 로그를 예외와 포맷 파라미터 모두와 함께 남긴다. */
    public void errorSys(String message, Throwable t, Object... args) {
        logWithMdc(SYS_LOG_TYPE, () -> log.error(message, args, t));
    }

    /* ============================== BIZ 로그 영역 ============================== */

    /** 비즈니스(debug) 로그를 남긴다. */
    public void debugBiz(String message) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.debug(message));
    }

    /** 비즈니스(debug) 로그를 포맷 파라미터와 함께 남긴다. */
    public void debugBiz(String message, Object... args) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.debug(message, args));
    }

    /** 비즈니스(info) 로그를 남긴다. */
    public void infoBiz(String message) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.info(message));
    }

    /** 비즈니스(info) 로그를 포맷 파라미터와 함께 남긴다. */
    public void infoBiz(String message, Object... args) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.info(message, args));
    }

    /** 비즈니스(warn) 로그를 남긴다. */
    public void warnBiz(String message) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.warn(message));
    }

    /** 비즈니스(warn) 로그를 포맷 파라미터와 함께 남긴다. */
    public void warnBiz(String message, Object... args) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.warn(message, args));
    }

    /** 비즈니스(error) 로그를 남긴다. */
    public void errorBiz(String message) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.error(message));
    }

    /** 비즈니스(error) 로그를 포맷 파라미터와 함께 남긴다. */
    public void errorBiz(String message, Object... args) {
        logWithMdc(BIZ_LOG_TYPE, () -> log.error(message, args));
    }

    /* ============================== 내부 공통 처리 ============================== */

    /**
     * logtype을 MDC에 지정한 뒤 로깅을 수행하고, 이후 MDC를 복원한다.
     *
     * @param logType 'biz' 또는 'sys'
     * @param runnable 로그를 실행하는 람다
     */
    private void logWithMdc(String logType, Runnable runnable) {
        Map<String, String> contextSnapshot = MDC.getCopyOfContextMap();
        try {
            MDC.put(LOG_TYPE_KEY, logType);
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
