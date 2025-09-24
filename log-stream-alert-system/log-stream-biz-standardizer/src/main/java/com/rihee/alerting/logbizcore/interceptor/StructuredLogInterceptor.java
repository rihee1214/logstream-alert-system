package com.rihee.alerting.logbizcore.interceptor;

import static com.rihee.alerting.common.constant.DefaultValues.B3HEADER_SAMPLED_DEFAULT;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.NAME;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.PARENT_SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.SPAN_ID;
import static com.rihee.alerting.common.constant.logging.StructuredLogFields.TRACE_ID;
import static com.rihee.alerting.common.constant.observability.ObservabilityDefaultFields.FLAGS;
import static com.rihee.alerting.common.constant.observability.ObservabilityDefaultFields.SAMPLED;

import com.rihee.alerting.common.constant.B3Header;
import com.rihee.alerting.common.constant.logging.LogType;
import com.rihee.alerting.logbizcore.log.StructuredLogger;
import com.rihee.alerting.logbizcore.log.StructuredLoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * {@code StructuredLogInterceptor}는 모든 HTTP 요청에 대해 traceId, spanId, parentSpanId를 자동으로 설정하고,
 * Structured Logging을 위한 MDC 구성을 보장하는 시스템 전용 인터셉터입니다.
 *
 * <p>외부에서 들어오는 traceId 및 parentSpanId는 고정된 유효성 정책에 따라 검증되며,
 * 유효하지 않은 경우 자동으로 재생성되거나 무시됩니다. 이는 시스템 전체의 추적 ID 통일성을 유지하기 위함입니다.
 *
 * <p>다음 조건을 기준으로 유효성을 판단합니다:
 * <ul>
 *   <li><b>traceId</b>: 요청 흐름 전반을 식별하기 위한 ID (16진수 문자열, 32의 배수 길이 권장)</li>
 *   <li><b>spanId</b>: 단일 작업 또는 호출 단위를 식별하는 ID (16의 배수 길이 권장)</li>
 *   <li><b>parentSpanId</b>: 이전 호출의 spanId. 유효하지 않으면 무시됨</li>
 * </ul>
 *
 * <p>생성된 ID는 MDC에 저장되며, 로그 수집기나 추적 시스템에서 활용할 수 있도록 구조화된 로그로 출력됩니다.</p>
 *
 * <p><b>정책 고정:</b> 생성 및 검증 정책은 코드 내부에 캡슐화되며 외부에서 재정의할 수 없습니다.</p>
 *
 * <p><b>B3 호환성:</b> 본 인터셉터는 Zipkin B3 헤더 규격에 따라
 * traceId, spanId, parentSpanId, sampled, flags 필드를 지원하며,
 * 다른 추적 시스템과의 연동을 고려한 통일된 필드 구조를 유지합니다.</p>
 */
public final class StructuredLogInterceptor implements HandlerInterceptor {

  // 모든 섹터에서 잡히지 않은 Exception을 afterCompletion 에서 로그로 찍어내기 위한 용도
  private static final StructuredLogger logger
      = StructuredLoggerFactory.getLogger(StructuredLogInterceptor.class);
  // traceId, spanId에 적합하지 않는 문자가 있는지 확인하기 위한 패턴
  private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-f]+$");

  // 요청 대상 메서드에 들어있는 spanLabel 매핑 정보가 담겨있는 레지스트리
  private final SpanLabelRegistry registry;

  private final int traceIdMultiplier;
  private final int spanIdMultiplier;

  /**
   * MDC 자동 생성 및 traceId/spanId 생성을 담당하는 로깅 인터셉터의 기본 생성자입니다.
   *
   * <p>이 생성자는 {@link SpanLabelRegistry}를 통해 각 메서드에 지정된 spanLabel을 추출하고,
   * 설정된 배수만큼의 traceId 및 spanId를 생성하여 MDC(Mapped Diagnostic Context)에 자동으로 삽입합니다.
   * 이는 추적성(log traceability)을 강화하고 로그 분석 시 흐름 파악을 용이하게 하기 위한 목적입니다.</p>
   *
   * <p>{@code traceIdMultiplier}는 UUID 단위를 몇 배로 연결하여 traceId의 길이를 확장할지 결정하며,
   * {@code spanIdMultiplier}는 무작위 long 값을 몇 개 연결하여 spanId의 길이를 조정할지를 결정합니다.
   * 이 두 값은 시스템 간 고유성 보장이나 추적 단위별 식별력을 강화할 수 있도록 설계 유연성을 제공합니다.</p>
   *
   * @param registry          요청 대상 메서드에 선언된 spanLabel 정보를 제공하는 레지스트리
   * @param traceIdMultiplier traceId 생성을 위한 UUID 배수 (예: 2 → 64자리 traceId)
   * @param spanIdMultiplier  spanId 생성을 위한 random long 배수 (예: 2 → 32자리 hex spanId)
   */
  public StructuredLogInterceptor(SpanLabelRegistry registry,
                                    int traceIdMultiplier,
                                    int spanIdMultiplier) {
    this.registry = registry;
    this.traceIdMultiplier = traceIdMultiplier;
    this.spanIdMultiplier = spanIdMultiplier;
  }

  /**
   * HTTP 요청 시작 시, 로그 및 추적을 위한 정보를 MDC에 설정합니다.
   *
   * <p>다음 정보를 MDC에 설정합니다:
   * <ul>
   *   <li><b>{@code traceId}</b>: 요청 헤더의 값이 유효하면 사용, 유효하지 않거나 누락 시 새로 생성됨
   *     <ul><li>형식: 32 이상의 길이를 가진, 32의 배수인 소문자 hex 문자열</li></ul>
   *   </li>
   *   <li><b>{@code spanId}</b>: {@link SpanLabelRegistry}에 매핑된 라벨 기반으로 생성되며,
   *     <ul><li>형식: 16 이상의 길이를 가진, 16의 배수인 소문자 hex 문자열</li></ul>
   *   </li>
   *   <li><b>{@code parentSpanId}</b>: 요청 헤더에 존재하는 {@code spanId}가 유효하면 상위 스팬으로 설정</li>
   *   <li><b>{@code serviceName}</b>, {@code hostName}, {@code containerName} 등 시스템 정보 추가</li>
   * </ul>
   *
   * <p>모든 값은 MDC에 설정되어 구조화 로그에 포함되며, 로그 추적 및 수집 시스템에서 활용됩니다.</p>
   */
  @Override
  public boolean preHandle(HttpServletRequest request,
          @NonNull HttpServletResponse response, @NonNull Object handler) {
    // 요청 헤더 기반 traceId, parentSpanId, spanId 세팅
    String traceId = request.getHeader(B3Header.TRACE_ID.getHeaderName());

    // TRACE_ID가 형식에 맞으면 그대로 사용하고, 그렇지 않으면 새로 생성, SPAN_ID는 새로 생성
    if (isNeedNewTraceId(traceId)) {
      traceId = generateTraceId();
    }
    MDC.put(TRACE_ID.getFieldName(), traceId);
    MDC.put(SPAN_ID.getFieldName(), generateSpanId());

    // 헤더를 통해 들어온 SPAN_ID가 적절하면 PARENT_SPAN_ID로 사용
    String spanId = request.getHeader(B3Header.SPAN_ID.getHeaderName());
    MDC.put(PARENT_SPAN_ID.getFieldName(), spanId);

    //
    if (handler instanceof HandlerMethod handlerMethod) {
      registry.findLabel(handlerMethod.getMethod())
              .ifPresent(label -> MDC.put(NAME.getFieldName(), label));
    }

    String sampled = request.getHeader(B3Header.SAMPLED.getHeaderName());
    String flags = request.getHeader(B3Header.FLAGS.getHeaderName());

    // TODO 이 항목들은 어떻게 처리할지 고민해야한다. (로그 수집을 위한 요소가 아닌 관측 가능성을 위한 요소)
    MDC.put(SAMPLED.getFieldName(), StringUtils.hasText(sampled) ? sampled
                                                          : B3HEADER_SAMPLED_DEFAULT.getValue());
    if (StringUtils.hasText(flags)) {
      MDC.put(FLAGS.getFieldName(), flags);
    }

    // response에 현재 사용중인 TraceId를 넣어주어, 바뀌더라도 추적이 가능하도록 처리
    if (!response.isCommitted()) {
      response.setHeader(B3Header.TRACE_ID.getHeaderName(), traceId);
    }
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
  public void afterCompletion(@NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
    // 요청 중 예외 발생시 StructuredLogging처리
    if (ex != null) {
      logger.error(LogType.SYS, "Exception during request", ex);
    }
    // MDC 정리 (thread-local 메모리 누수 방지)
    MDC.clear();
  }

  // === 헬퍼 메서드 ===
  /**
   * 현재 요청과 매핑된 {@link SpanLabelRegistry}를 반환합니다.
   *
   * <p>서브 클래스 또는 하위 구현체에서 메서드에 연결된 spanLabel 정보를 조회할 때 사용됩니다.</p>
   *
   * @return {@link SpanLabelRegistry} 인스턴스
   */
  private SpanLabelRegistry getRegistry() {
    return this.registry;
  }

  /**
   * 요청 헤더에서 전달된 traceId를 기반으로 최종 traceId 값을 생성합니다.
   *
   * <p>일반적으로 traceId가 비어있거나 유효하지 않은 경우 새 traceId를 생성합니다.
   * 이 메서드는 {@code traceIdMultiplier} 설정값에 따라 UUID를 여러 개 연결하여 traceId의 길이를 늘립니다.
   * 이를 통해 충돌 확률을 줄이고 시스템 간 구분력을 높일 수 있습니다.
   *
   * <p>서브 클래스는 traceId 생성 정책이나 포맷(예: 접두어 포함, 시간 기반 등)을 자유롭게 재정의할 수 있습니다.</p>
   *
   * @return 최종적으로 사용될 traceId 문자열 (예: 32 * multiplier 길이의 UUID hex 문자열)
   */
  private String generateTraceId() {
    StringBuilder sb = new StringBuilder(traceIdMultiplier * 32);
    for (int i = 0; i < traceIdMultiplier; i++) {
      sb.append(UUID.randomUUID().toString().replace("-", ""));
    }
    return sb.toString();
  }


  /**
   * 요청 헤더의 기존 spanId와 메서드의 spanLabel을 기반으로 새로운 spanId를 생성합니다.
   *
   * <p>{@code spanIdMultiplier} 설정값에 따라 무작위 long 값을 여러 개 16진수로 변환하여 연결된 형태로 spanId를 구성합니다.
   * 일반적으로 trace 트리의 계층 구조를 명확히 하기 위해 parentSpanId와의 연결성을 고려하거나,
   * spanId를 고유하게 만들기 위해 이와 같은 방식으로 생성합니다.
   *
   * <p>서브 클래스는 spanId의 구조(예: 서비스명-업무명-순번) 또는 연결 방식을 정의할 수 있으며,
   * 형식의 일관성과 길이 보장이 필요한 경우 고정 포맷(hex padding)도 고려할 수 있습니다.</p>
   *
   * @return 최종적으로 로깅에 사용될 spanId 문자열 (예: 16 * multiplier 길이의 hex 문자열)
   */
  private String generateSpanId() {
    StringBuilder sb = new StringBuilder(spanIdMultiplier * 16);
    for (int i = 0; i < spanIdMultiplier; i++) {
      long randomLong = ThreadLocalRandom.current().nextLong();
      sb.append(String.format("%016x", randomLong));
    }
    return sb.toString();
  }

  /**
   * 지정된 값이 유효하지 않은 traceId인지 검사합니다.
   * 유효 기준: 길이가 32의 배수이고, 소문자 hex 문자로만 구성된 문자열
   *
   * @param traceId 검증 대상 traceId
   * @return 유효하면 false
   */
  private boolean isNeedNewTraceId(String traceId) {
    return !StringUtils.hasText(traceId)
        || traceId.length() < 32
        || traceId.length() % 32 != 0
        || !HEX_PATTERN.matcher(traceId).matches();
  }
}
