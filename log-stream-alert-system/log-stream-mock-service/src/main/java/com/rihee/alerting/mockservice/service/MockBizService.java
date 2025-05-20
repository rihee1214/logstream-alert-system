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
   * 단순 비즈니스 로직을 수행하는 메서드입니다.
   *
   * <p>구현체에서는 로그 출력 또는 mock 데이터 생성 등의 처리를 수행할 수 있으며,
   * 호출 시 structured logging 체계가 정상적으로 작동하는지 검증하는 목적으로도 활용됩니다.
   *
   * @return 처리 결과 문자열
   */
  String doSomething();
}
