package com.rihee.alerting.mockservice.service;

/**
 * {@code MockBizService}는 mock-service의 간단한 비즈니스 로직 수행을 위한 인터페이스입니다.
 *
 * <p>이 인터페이스는 컨트롤러 계층에서 비즈니스 계층의 호출 흐름을 분리하고,
 * 테스트 환경이나 샘플 서비스 구현에서 유연한 구조를 제공하기 위해 정의되었습니다.
 *
 * <p>실제 구현체에서는 단순 로그 출력, 테스트 응답 반환 등의 역할을 수행할 수 있으며,
 * 복잡한 수준의 로직 분기를 실행할 수도 있다.<br>
 * 그리고 로깅 트레이싱 구조(MDC, spanId 등)의 흐름 유지를 위한 테스트 도구로도 활용될 수 있습니다.
 *
 * @author 리희
 * @since 1.0
 */
public interface MockBizService {

  /**
   * "Simple Call" 시나리오에서 단순 비즈니스 로직을 수행하는 메서드입니다.
   *
   * <p>로그 출력 또는 mock 데이터 반환 등, 최소한의 비즈니스 처리를 테스트할 수 있으며,
   * structured logging 체계가 정상적으로 작동하는지 검증하는 목적으로도 사용됩니다.
   * </p>
   *
   * @return 처리 결과 문자열
   */
  String doSimpleSomething();

  /**
   * "Middle Layer Call" 또는 "Branch Call" 시나리오에서 중간 mock 서비스를 구성하는 메서드입니다.
   *
   * <p>외부 mock 호출 또는 DB 조회 시뮬레이션을 포함한 중간 단계 로직을 구성하며,
   * 다단계 mock 흐름에서 흐름의 연속성과 응답 구성을 검증하는 데 사용됩니다.
   * </p>
   *
   * @return 처리 결과 문자열
   */
  String doMiddleSomething();

  /**
   * "Branch Call" 시나리오의 시작점에서 호출되는 메서드입니다.
   *
   * <p>여러 mock 서비스를 병렬 호출하거나 흐름을 분기시키는 테스트 구성을 할 수 있으며,
   * 각 분기 이후 응답 조합 흐름 검증 목적에 활용됩니다.
   * </p>
   *
   * @return 처리 결과 문자열
   */
  String doBranchSomething();

  /**
   * "Multi Layer Call" 시나리오의 시작점에서 호출되는 메서드입니다.
   *
   * <p>하나의 요청이 연속된 mock 서비스들을 계층적으로 호출하는 구조를 통해,
   * 전체 흐름의 연쇄성과 단계별 로깅 흐름이 일관되게 유지되는지 검증할 수 있습니다.
   * </p>
   *
   * @return 처리 결과 문자열
   */
  String doMultiLayerSomething();
}
