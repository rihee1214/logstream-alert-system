package com.rihee.alerting.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>비즈니스(Biz) 및 시스템(Sys) 로그를 구분해서 기록할 수 있는
 * {@link StructuredLogger} 인스턴스를 생성하기 위한 팩토리 클래스입니다.</p>
 *
 * <p>
 * 이 팩토리를 통해 생성된 {@code StructuredLogger}는 로그 타입에 따라
 * 자동으로 MDC 정보를 설정하고, 로그 호출 클래스를 명확히 남길 수 있도록 설계되었습니다.
 * </p>
 *
 * <h2>주요 목적</h2>
 * <ul>
 *     <li>Biz, Sys 로그를 구분하여 분리 수집 및 모니터링 가능</li>
 *     <li>MDC 자동 관리로 일관된 로그 필드 유지</li>
 *     <li>SLF4J Logger를 wrapping하여 Class 정보 보존</li>
 * </ul>
 *
 * <p>StructuredLogger 사용 방법은 {@link StructuredLogger} 문서를 참고하세요.</p>
 *
 * @author 리희
 * @since 1.0
 */
public class StructuredLoggerFactory {

    // 생성자를 이용한 생성 금지
    private StructuredLoggerFactory(){}

    public static StructuredLogger getLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new StructuredLoggerImpl(logger);
    }
}
