package com.rihee.alerting.common.interceptor;

import static com.rihee.alerting.common.constant.DefaultValues.B3HEADER_SAMPLED_DEFAULT;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.META;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.PARENT_SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.SPAN_ID;
import static com.rihee.alerting.common.log.constant.StructuredLogProperties.TRACE_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rihee.alerting.common.constant.B3Header;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import io.micrometer.common.lang.NonNullApi;
import io.micrometer.common.lang.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP 요청마다 MDC (Mapped Diagnostic Context)를 세팅하여 로깅에 필요한 메타데이터를 구성하고,
 * 요청 처리 후 MDC를 정리하는 인터셉터입니다.
 *
 * <p>- 요청 헤더의 traceId, parentSpanId, spanId를 기반으로 트레이싱 정보를 구성합니다.
 * - {@link SpanLabelRegistry}를 통해 메서드에 매핑된 spanLabel을 활용하여 spanId를 생성합니다.
 * - 공통적으로 서비스 이름, 호스트 이름, 컨테이너 이름을 MDC에 포함합니다.
 * </p>
 *
 * <p><b>사용 예시</b>: API 요청 로깅을 위한 선행 인터셉터로 활용</p>
 */
@NonNullApi
public class StructuredLogInterceptor implements HandlerInterceptor {

  // Json 생성을 위한 ObjectNode를 생성하기 위한 요소.
  private static final ObjectMapper mapper = new ObjectMapper();
  // 모든 섹터에서 잡히지 않은 Exception을 afterCompletion 에서 로그로 찍어내기 위한 용도
  private static final StructuredLogger logger
                            = StructuredLoggerFactory.getLogger(StructuredLogInterceptor.class);

  // 요청 대상 메서드에 들어있는 spanLabel 매핑 정보가 담겨있는 레지스트리
  private final SpanLabelRegistry registry;

  // MDC에 기본 세팅될 서비스, 호스트, 컨테이너 정보
  private final String serviceName;

  /**
   * MDC 자동 생성 작업을 하는 로깅 인터셉터를 위한 기본 생성자입니다.
   *
   * @param registry 요청 대상 메서드에 들어있는 spanLabel 매핑 정보가 담겨있는 레지스트리.
   * @param serviceName MDC에 기본 세팅될 서비스 정보.
   */
  public StructuredLogInterceptor(SpanLabelRegistry registry,
                                    String serviceName) {
    this.registry = registry;
    this.serviceName = serviceName;
  }

  /**
   * HTTP 요청 시작 시, 로그 및 추적을 위한 정보를 MDC에 설정합니다.
   *
   * <p>다음과 같은 정보를 MDC에 등록합니다:</p>
   * <ul>
   *     <li><b>{@code traceId}</b>: 요청 헤더에 존재하면 사용하고, 없으면 새로 생성</li>
   *     <li><b>{@code spanId}</b>: {@link SpanLabelRegistry}를 통해 핸들러 메서드에 매핑된 라벨 기반으로 생성</li>
   *     <li><b>{@code parentSpanId}</b>: 요청 헤더에 {@code spanId}가 있으면 상위 span으로 설정</li>
   *     <li><b>기본 필드</b>: {@code serviceName}, {@code hostName}, {@code containerName}도 함께 설정</li>
   *     <li><b>{@code meta}</b>: {@code sampled}, {@code flags} 값을 JSON 형태로 구조화하여 추가</li>
   * </ul>
   *
   * @param request  현재 HTTP 요청 객체
   * @param response 현재 HTTP 응답 객체
   * @param handler  실제 요청을 처리할 컨트롤러 핸들러
   * @return 항상 {@code true} 반환하여 DispatcherServlet의 이후 체인을 계속 진행
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                                                        Object handler) {
    // 요청 헤더 기반 traceId, parentSpanId, spanId 세팅
    String traceId = request.getHeader(B3Header.TRACE_ID.getHeaderName());
    String spanId = request.getHeader(B3Header.SPAN_ID.getHeaderName());
    MDC.put(TRACE_ID.getName(), generateTraceId(traceId));
    if (StringUtils.hasText(spanId)) {
      MDC.put(PARENT_SPAN_ID.getName(), spanId);
    }
    if (handler instanceof HandlerMethod handlerMethod) {
      registry.findLabel(handlerMethod.getMethod())
              .ifPresent(label -> MDC.put(SPAN_ID.getName(), generateSpanId(spanId, label)));
    }

    // Meta에 로그 추적기를 위한 선택 헤더 추가.
    String sampled = request.getHeader(B3Header.SAMPLED.getHeaderName());
    String flags = request.getHeader(B3Header.FLAGS.getHeaderName());

    ObjectNode json = mapper.createObjectNode();
    json.put("sampled", StringUtils.hasText(sampled) ? sampled
                                                            : B3HEADER_SAMPLED_DEFAULT.getValue());
    if (StringUtils.hasText(flags)) {
      json.put("flags", flags);
    }
    MDC.put(META.getName(), json.toString());
    return true;
  }

  /**
   * 요청 처리 종료 작업 진행<br>
   * 다음 요청시 정확한 로깅을 위해 MDC 초기화 진행.
   *
   * @param request  current HTTP request
   * @param response current HTTP response
   * @param handler  the handler (or {@link HandlerMethod}) that started asynchronous
   *                 execution, for type and/or instance examination
   * @param ex       any exception thrown on handler execution, if any; this does not
   *                 include exceptions that have been handled through an exception resolver
   */
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                                Object handler, @Nullable Exception ex) {
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
  private String generateTraceId(String traceId) {
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
   * <p></p>NOTE: spanId는 "서비스명-spanLabel-번호" 형식임.
   * 해당 포맷이 바뀌면 테스트 코드{@code StructuredHttpServerTests}도 함께 수정할 것</p>
   *
   * @param spanId 요청 헤더에서 읽은 spanId
   * @return 새로 생성된 spanId
   */
  private String generateSpanId(String spanId, String spanLabel) {
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
