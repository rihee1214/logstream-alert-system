package com.rihee.alerting.common.log.aspect;

import static com.rihee.alerting.common.log.enums.StructuredLogProperties.TRACE_ID;
import static com.rihee.alerting.common.log.enums.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.enums.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.log.enums.StructuredLogProperties.SERVICE;
import static com.rihee.alerting.common.log.enums.StructuredLogProperties.HOST;
import static com.rihee.alerting.common.log.enums.StructuredLogProperties.CONTAINER;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

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
    private final String serviceName;
    private final String hostName;
    private final String containerName;

    /**
     * 환경 변수 및 HttpServletRequest를 주입받아 초기화한다.
     *
     * @param request 현재 요청 객체
     * @param serviceName 서비스 이름
     * @param hostName 호스트 이름
     * @param containerName 컨테이너 이름
     */
    public StructuredMdcAspect(HttpServletRequest request,
                               @Value("${service.name:unknown-service}") String serviceName,
                               @Value("${host.name:unknown-host}") String hostName,
                               @Value("${container.name:unknown-container}") String containerName) {
        this.request = request;
        this.serviceName = serviceName;
        this.hostName = hostName;
        this.containerName = containerName;
    }

    /**
     * &#064;StructuredRestController  가 붙은 클래스에 대해
     * 요청 흐름마다 MDC에 traceId, spanId, 기타 부가 정보를 세팅하고,
     * 요청 종료 후 반드시 MDC를 초기화한다.
     *
     * <p>핵심 흐름:</p>
     * <ol>
     *     <li>MDC에 서비스 기본 정보 삽입 (service, host, container)</li>
     *     <li>요청 헤더로부터 traceId, spanId를 읽어 MDC에 추가</li>
     *     <li>핸들러 메서드 실행</li>
     *     <li>요청 완료 후 MDC 원상 복구</li>
     * </ol>
     *
     * @param pjp 현재 진행 중인 조인 포인트
     * @return 핸들러 메서드 실행 결과
     * @throws Throwable 핸들러 실행 중 발생하는 예외
     */
    @Around("@within(com.rihee.alerting.common.log.annotation.StructuredRestController)")
    public Object aroundStructuredController(ProceedingJoinPoint pjp) throws Throwable {
        // [1] MDC 기본 세팅
        MDC.put(SERVICE.getName(), serviceName);
        MDC.put(HOST.getName(), hostName);
        MDC.put(CONTAINER.getName(), containerName);

        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        // [2] 요청 헤더 기반 traceId, spanId 세팅
        try {
            // 여기서 Http Header 읽어서 MDC에 넣는다
            String traceId = request.getHeader(TRACE_ID.getName());
            String spanId = request.getHeader(SPAN_ID.getName());
            MDC.put(TRACE_ID.getName(), generateTraceId(traceId));
            if (StringUtils.hasText(spanId)) {
                MDC.put(PARENT_SPAN_ID.getName(), spanId);
            }
            MDC.put(SPAN_ID.getName(), generateSpanId(spanId));

            // [3] 실제 핸들러 실행
            return pjp.proceed();
        } finally {
            // [4] MDC 복원
            if (previousContext != null) {
                MDC.setContextMap(previousContext);
            } else {
                MDC.clear();
            }
        }
    }

    // === 헬퍼 메서드 ===

    /**
     * traceId가 존재하면 그대로 사용하고, 없으면 새로운 UUID를 생성한다.
     *
     * @param traceId 요청 헤더에서 읽은 traceId
     * @return 유효한 traceId
     */
    private String generateTraceId(String traceId){
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }

    /**
     * spanId가 존재하고 올바른 형식이면 서비스 이름과 함께 순번을 증가시킨 새 spanId를 생성하고,
     * 그렇지 않으면 서비스 이름과 함께 1번부터 시작하는 spanId를 생성한다.
     *
     * @param spanId 요청 헤더에서 읽은 spanId
     * @return 새로 생성된 spanId
     */
    private String generateSpanId(String spanId){
        if (StringUtils.hasText(spanId)) {
            String[] parts = spanId.split("-");
            try {
                if (parts.length == 2) {
                    int oldSeq = Integer.parseInt(parts[1]);
                    int newSeq = oldSeq + 1;
                    return serviceName + "-" + newSeq;
                }
            } catch (NumberFormatException ignore) {
                // spanId 형식이 올바르지 않아 새로운 spanId를 작성.
            }
        }
        return serviceName + "-1";
    }

}
