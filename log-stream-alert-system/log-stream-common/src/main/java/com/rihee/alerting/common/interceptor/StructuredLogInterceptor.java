package com.rihee.alerting.common.interceptor;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.enums.LogType;
import io.micrometer.common.lang.NonNullApi;
import io.micrometer.common.lang.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

import static com.rihee.alerting.common.log.enums.StructuredLogProperties.*;

/**
 * HTTP 요청마다 MDC (Mapped Diagnostic Context)를 세팅하여 로깅에 필요한 메타데이터를 구성하고,
 * 요청 처리 후 MDC를 정리하는 인터셉터입니다.
 * <p>
 * - 요청 헤더의 traceId, parentSpanId, spanId를 기반으로 트레이싱 정보를 구성합니다.
 * - {@link SpanLabelRegistry}를 통해 메서드에 매핑된 spanLabel을 활용하여 spanId를 생성합니다.
 * - 공통적으로 서비스 이름, 호스트 이름, 컨테이너 이름을 MDC에 포함합니다.
 * </p>
 *
 * <p><b>사용 예시</b>: API 요청 로깅을 위한 선행 인터셉터로 활용</p>
 */
@NonNullApi
public class StructuredLogInterceptor implements HandlerInterceptor {

    // 모든 섹터에서 잡히지 않은 Exception을 afterCompletion 에서 로그로 찍어내기 위한 용도
    private static final StructuredLogger logger = StructuredLoggerFactory.getLogger(StructuredLogInterceptor.class);

    // 요청 대상 메서드에 들어있는 spanLabel 매핑 정보가 담겨있는 레지스트리
    private final SpanLabelRegistry registry;

    // MDC에 기본 세팅될 서비스, 호스트, 컨테이너 정보
    private final String serviceName;
    /**
     * @param registry      요청 대상 메서드에 들어있는 spanLabel 매핑 정보가 담겨있는 레지스트리
     * @param serviceName   MDC에 기본 세팅될 서비스 정보
     */
    public StructuredLogInterceptor(SpanLabelRegistry registry,
                                    String serviceName) {
        this.registry = registry;
        this.serviceName = serviceName;
    }

    /**
     * HTTP 요청 시작 시 MDC에 로깅 및 트레이싱을 위한 정보를 세팅합니다.
     *
     * <ul>
     *     <li>{@code traceId}: 요청 헤더에 존재하면 그대로 사용, 없으면 새로 생성</li>
     *     <li>{@code spanId}: {@link SpanLabelRegistry}에서 해당 메서드에 대한 spanLabel을 조회 후, 기존 spanId를 이어받아 생성</li>
     *     <li>기본적으로 {@code serviceName}, {@code hostName}, {@code containerName}도 함께 세팅</li>
     * </ul>
     *
     * @param request  현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler  실제 요청을 처리할 핸들러 객체
     * @return 항상 {@code true} 반환하여 요청을 계속 진행
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // [1] MDC 기본 세팅
        MDC.put(SERVICE.getName(), serviceName);

        // [2] 요청 헤더 기반 traceId, parentSpanId, spanId 세팅
        String traceId = request.getHeader(TRACE_ID.getName());
        String spanId = request.getHeader(SPAN_ID.getName());
        MDC.put(TRACE_ID.getName(), generateTraceId(traceId));
        if (StringUtils.hasText(spanId)) {
            MDC.put(PARENT_SPAN_ID.getName(), spanId);
        }
        if (handler instanceof HandlerMethod handlerMethod) {
            registry.findLabel(handlerMethod.getMethod())
                    .ifPresent(label -> MDC.put(SPAN_ID.getName(), generateSpanId(spanId, label)));
        }
        return true;
    }

    /**
     * 요청 처리 종료 작업 진행<br>
     * 다음 요청시 정확한 로깅을 위해 MDC 초기화 진행.
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param ex any exception thrown on handler execution, if any; this does not
     * include exceptions that have been handled through an exception resolver
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        // 요청 중 예외 발생시 StructuredLogging처리
        if (ex != null) {
            logger.error(LogType.SYS, "Exception during request", ex);
        }
        // MDC 정리 (thread-local 메모리 누수 방지)
        MDC.clear();
    }

    // === 헬퍼 메서드 ===

    // traceId 설정 구역
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

    // spanId 설정 구역
    /**
     * spanId가 존재하고 올바른 형식이면 서비스 이름과 함께 순번을 증가시킨 새 spanId를 생성하고,
     * 그렇지 않으면 서비스 이름과 함께 1번부터 시작하는 spanId를 생성한다.
     *
     * @param spanId 요청 헤더에서 읽은 spanId
     * @return 새로 생성된 spanId
     */
    private String generateSpanId(String spanId, String spanLabel){
        if (StringUtils.hasText(spanId)) {
            String[] parts = spanId.split("-");
            try {
                if (parts.length > 2) {
                    // 예: service-spanlabel-3 같은 형식일 때만 처리
                    int oldSeq = Integer.parseInt(parts[parts.length - 1]);
                    return serviceName + "-" + spanLabel + "-" + (oldSeq + 1);
                }
            } catch (NumberFormatException ignore) {
                // spanId 형식이 올바르지 않아 새로운 spanId를 작성.
            }
        }
        return serviceName + "-" + spanLabel + "-1";
    }
}
