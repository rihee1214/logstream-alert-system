package com.rihee.alerting.common.interceptor;

import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.constant.LogType;
import io.micrometer.common.lang.NonNullApi;
import java.util.UUID;
import org.springframework.util.StringUtils;

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
public class DefaultStructuredLogInterceptor extends AbstractStructuredLogInterceptor {

  // 모든 섹터에서 잡히지 않은 Exception을 afterCompletion 에서 로그로 찍어내기 위한 용도
  private static final StructuredLogger logger
                        = StructuredLoggerFactory.getLogger(DefaultStructuredLogInterceptor.class);

  /**
   * MDC 자동 생성 작업을 하는 로깅 인터셉터를 위한 기본 생성자입니다.
   *
   * @param registry 요청 대상 메서드에 들어있는 spanLabel 매핑 정보가 담겨있는 레지스트리.
   * @param serviceName MDC에 기본 세팅될 서비스 정보.
   */
  public DefaultStructuredLogInterceptor(SpanLabelRegistry registry,
      String serviceName) {
    super(registry, serviceName);
  }

  // === 헬퍼 메서드 ===

  // traceId 설정 구역

  /**
   * traceId가 존재하면 그대로 사용하고, 없으면 새로운 UUID를 생성한다.
   *
   * @param traceId 요청 헤더에서 읽은 traceId
   * @return 유효한 traceId
   */
  @Override
  protected String generateTraceId(String traceId) {
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
  @Override
  protected String generateSpanId(String spanId, String spanLabel) {
    if (StringUtils.hasText(spanId)) {
      String[] parts = spanId.split("-");
      try {
        if (parts.length > 2) {
          // 예: service-spanlabel-3 같은 형식일 때만 처리
          int oldSeq = Integer.parseInt(parts[parts.length - 1]);
          return getServiceName() + "-" + spanLabel + "-" + (oldSeq + 1);
        }
      } catch (NumberFormatException ignore) {
        logger.warn(LogType.SYS, "상대방 시스템에서 들어온 SpanId 형식이 표준에 어긋납니다.\n spanId : {}", spanId);
      }
    }
    return getServiceName() + "-" + spanLabel + "-1";
  }

  /**
   * 기본 구현에서는 전달받은 {@code spanId}를 그대로 {@code parentSpanId}로 사용합니다.
   *
   * <p>추적 흐름을 유지하기 위해 상위 span ID를 별도 가공 없이 그대로 전달합니다.
   * 커스텀 구현에서는 이 메서드를 오버라이드하여 포함 여부나 값 가공을 제어할 수 있습니다.</p>
   *
   * @param spanId 요청 헤더에서 전달된 spanId
   * @return 그대로 전달된 spanId
   */
  @Override
  protected String generateParentSpanId(String spanId) {
    return spanId;
  }
}
