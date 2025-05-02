package com.rihee.alerting.common.log;

import java.util.Map;

/**
 * {@code StructuredLogger}는 시스템 로그와 비즈니스 로그를 구분하여 structured logging을 지원하는 인터페이스입니다.
 * <p>
 * 이 인터페이스는 로그 타입(sys/biz)을 명확히 구분하며,
 * 로그 레벨별로 메시지, 예외, 포맷 파라미터를 유연하게 처리할 수 있도록 다양한 오버로드 메서드를 제공합니다.
 * </p>
 *
 * <p>
 * 로그 호출 시 내부적으로 MDC를 활용하여 {@code log_type}, {@code traceId}, {@code spanId}, {@code parentSpanId} 등
 * 컨텍스트 정보를 자동으로 설정 및 정리합니다.
 * 이를 통해 분산 환경에서도 로그 추적성을 보장합니다.
 * </p>
 *
 * <p><b>사용 예시</b>: {@code StructuredLoggerFactory.getLogger(MyClass.class)} 를 통해 생성</p>
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
     * @param message 출력할 로그 메시지
     */
    void debugSys(String message);
    /**
     * 시스템 로그(DEBUG 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param args    포맷 인자
     */
    void debugSys(String message, Object... args);
    /**
     * 시스템 로그(DEBUG 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     */
    void debugSys(String message, Throwable t);
    /**
     * 시스템 로그(DEBUG 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     * @param args    포맷 인자
     */
    void debugSys(String message, Throwable t, Object... args);

    /**
     * 시스템 로그(INFO 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void infoSys(String message);
    /**
     * 시스템 로그(INFO 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param args    포맷 인자
     */
    void infoSys(String message, Object... args);
    /**
     * 시스템 로그(INFO 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     */
    void infoSys(String message, Throwable t);
    /**
     * 시스템 로그(INFO 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     * @param args    포맷 인자
     */
    void infoSys(String message, Throwable t, Object... args);

    /**
     * 시스템 로그(WARN 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void warnSys(String message);
    /**
     * 시스템 로그(WARN 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param args    포맷 인자
     */
    void warnSys(String message, Object... args);
    /**
     * 시스템 로그(WARN 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     */
    void warnSys(String message, Throwable t);
    /**
     * 시스템 로그(WARN 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     * @param args    포맷 인자
     */
    void warnSys(String message, Throwable t, Object... args);

    /**
     * 시스템 로그(ERROR 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void errorSys(String message);
    /**
     * 시스템 로그(ERROR 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param args    포맷 인자
     */
    void errorSys(String message, Object... args);
    /**
     * 시스템 로그(ERROR 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     */
    void errorSys(String message, Throwable t);
    /**
     * 시스템 로그(ERROR 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     * @param t       예외 객체
     * @param args    포맷 인자
     */
    void errorSys(String message, Throwable t, Object... args);

    // BIZ 로그
    /**
     * 비즈니스 로그(DEBUG 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void debugBiz(String message);
    /**
     * 비즈니스 로그(DEBUG 레벨)를 포맷 문자열과 인자를 이용해 기록합니다.
     *
     * @param message 포맷 문자열
     * @param args    포맷 인자
     */
    void debugBiz(String message, Object... args);

    /**
     * 비즈니스 로그(INFO 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void infoBiz(String message);
    /**
     * 비즈니스 로그(INFO 레벨)를 포맷 문자열과 인자를 이용해 기록합니다.
     *
     * @param message 포맷 문자열
     * @param args    포맷 인자
     */
    void infoBiz(String message, Object... args);

    /**
     * 비즈니스 로그(WARN 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void warnBiz(String message);
    /**
     * 비즈니스 로그(WARN 레벨)를 포맷 문자열과 인자를 이용해 기록합니다.
     *
     * @param message 포맷 문자열
     * @param args    포맷 인자
     */
    void warnBiz(String message, Object... args);

    /**
     * 비즈니스 로그(ERROR 레벨)를 기록합니다.
     *
     * @param message 출력할 로그 메시지
     */
    void errorBiz(String message);
    /**
     * 비즈니스 로그(ERROR 레벨)를 포맷 문자열과 인자를 이용해 기록합니다.
     *
     * @param message 포맷 문자열
     * @param args    포맷 인자
     */
    void errorBiz(String message, Object... args);
}
