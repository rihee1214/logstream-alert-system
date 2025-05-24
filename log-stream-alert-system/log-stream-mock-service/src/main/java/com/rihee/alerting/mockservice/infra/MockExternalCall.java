package com.rihee.alerting.mockservice.infra;

/**
 * {@code MockExternalCall}은 mock 서비스의 외부 호출을 추상화하는 인프라 포트 인터페이스입니다.
 *
 * <p>이 인터페이스는 mockup-request-flow 시나리오 내에서 외부 mock 서비스 호출을 표현하며,
 * 실제 구현체는 {@code WebClient} 등을 통해 HTTP 기반 요청을 수행합니다.
 * </p>
 *
 * <p>해당 포트는 다음과 같은 시나리오에서 사용됩니다:</p>
 * <ul>
 *   <li><strong>externalMiddleCall()</strong>: "middleBiz" mock 호출 (Branch, Multi Layer)</li>
 *   <li><strong>externalSimpleCall()</strong>: "simpleBiz" mock 호출 (Branch)</li>
 * </ul>
 *
 * <p><strong>⚠️ 실패 처리 주의사항:</strong></p>
 *
 * <p>현재 인터페이스는 정상 흐름 기준으로만 정의되어 있으며,
 * 호출 실패, 타임아웃, 예외 발생 등은 구현체 수준에서 처리되어야 합니다.
 * </p>
 *
 * @author 리희
 * @since 1.0
 */
public interface MockExternalCall {

  /**
   * "middleBiz" mock 서비스를 호출합니다.
   *
   * <p>Branch 또는 Multi Layer 시나리오 중간 단계에서 사용되며,
   * 외부 mock 시스템으로부터 응답을 수신합니다.
   * </p>
   *
   * @return 외부 호출 응답 문자열
   */
  String externalMiddleCall();

  /**
   * "simpleBiz" mock 서비스를 호출합니다.
   *
   * <p>외부 mock 시스템의 단순 응답 흐름을 검증할 수 있습니다.
   * </p>
   *
   * @return 외부 호출 응답 문자열
   */
  String externalSimpleCall();
}
