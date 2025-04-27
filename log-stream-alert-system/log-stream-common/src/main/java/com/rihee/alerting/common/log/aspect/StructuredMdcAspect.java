package com.rihee.alerting.common.log.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * {@link com.rihee.alerting.common.log.annotation.StructuredRestController}가 붙은 클래스의 요청 흐름에 대해
 * StructuredLogger용 MDC 세팅을 자동으로 처리하는 AOP Aspect.
 *
 * <p>
 * - HTTP 요청 헤더로부터 필요한 값을 추출하여 MDC에 주입한다.
 * - 요청 처리 후에는 MDC를 반드시 초기화(clean)하여 메모리 누수 및 스레드 간 오염을 방지한다.
 * </p>
 *
 * <h2>처리 흐름</h2>
 * <ol>
 *     <li>요청 진입 시: HttpServletRequest의 헤더 값 읽어 MDC에 저장</li>
 *     <li>핸들러 메서드 실행</li>
 *     <li>요청 완료 후: MDC 초기화</li>
 * </ol>
 *
 * <h2>주요 사용 목적</h2>
 * <ul>
 *     <li>로깅 시 traceId, spanId, service, container 등의 부가정보를 자동 삽입</li>
 *     <li>전체 시스템의 로그 구조 일관성 보장</li>
 *     <li>휴먼 에러를 최소화하고 개발 편의성 극대화</li>
 * </ul>
 *
 * @see com.rihee.alerting.common.log.annotation.StructuredRestController
 * @see org.slf4j.MDC
 */
@Aspect
@Component
public class StructuredMdcAspect {

    private final HttpServletRequest request;

    public StructuredMdcAspect(HttpServletRequest request) {
        this.request = request;
    }

    // TODO traceId 자동 생성 후 주입 및, spanId 자동 생성 필요
    // TODO 나머지 값들은 evironment 변수를 주입하여 세팅하도록 해야함 (service, host, container)
    @Around("@within(com.rihee.alerting.common.log.annotation.StructuredRestController)")
    public Object aroundStructuredController(ProceedingJoinPoint pjp) throws Throwable {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            // 여기서 Http Header 읽어서 MDC에 넣는다
            String traceId = request.getHeader("traceId");
            String spanId = request.getHeader("spanId");
            if (traceId != null) {
                MDC.put("traceId", traceId);
            }
            if (spanId != null) {
                MDC.put("spanId", spanId);
            }

            return pjp.proceed();
        } finally {
            if (previousContext != null) {
                MDC.setContextMap(previousContext);
            } else {
                MDC.clear();
            }
        }
    }

}
