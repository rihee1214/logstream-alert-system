package com.rihee.alerting.mockservice.config;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * {@code ControllerEndpointRegistry}는 Spring 애플리케이션의 모든 {@code @RequestMapping} 기반
 * 컨트롤러 경로(URI 패턴)를 수집하여 검증 가능한 형태로 보관하는 레지스트리 컴포넌트입니다.
 *
 * <p>이 컴포넌트는 Spring MVC의 {@link RequestMappingHandlerMapping}을 활용하여,
 * 실행 시점에 등록된 모든 컨트롤러 경로를 조회하고 내부 Set 구조에 저장합니다.
 *
 * <p>Mockup Scheduler와 같은 기능에서, 특정 경로가 실제 컨트롤러에 매핑되어 있는지를
 * 확인하고, 유효하지 않은 경로에 대한 등록을 사전에 차단하는 용도로 활용됩니다.
 *
 * <p>경로 수집은 애플리케이션 초기화 시점에 자동으로 수행되며, 수집된 URI 패턴은
 * 이후 {@link #isValidEndpoint(String)} 메서드를 통해 검증에 사용됩니다.
 *
 * <p>/mock/scheduler 하위 경로는 내부 컨트롤러이므로 등록 불가 대상에서 제외됩니다.
 * 이는 등록 가능한 요청 URI 필터링 시 내부 시스템 보호를 위한 목적입니다.
 *
 * @author 리희
 * @since 1.0
 */
@Component
public final class ControllerEndpointRegistry {

  private static final String EXCLUDE_PREFIX = "/mock/scheduler";

  // application 부팅시 등록된 모든 컨트롤러의 URI 패턴을 가지고 있는 handler
  private final RequestMappingHandlerMapping handlerMapping;
  // 요청한 URI가 실제 존재하는 서비스인지 확인하기 위한 자료
  private final Set<String> validEndpoints = new HashSet<>();
  // init 메서드를 개발자가 다시 호출하더라도, 재동작 하지 않게 막기 위한 요소
  private boolean isInitialed = false;

  /**
   * {@code ControllerEndpointRegistry}의 생성자입니다.
   *
   * @param handlerMapping Spring의 {@link RequestMappingHandlerMapping}으로,
   *                       현재 애플리케이션에 등록된 컨트롤러 경로 정보를 제공받기 위해 사용됩니다.`
   */
  @Autowired
  public ControllerEndpointRegistry(RequestMappingHandlerMapping handlerMapping) {
    this.handlerMapping = handlerMapping;
  }

  /**
   * Spring 컨텍스트 초기화 이후 실행되며, {@link RequestMappingHandlerMapping}을 통해
   * 현재 등록된 모든 컨트롤러의 URI 패턴 정보를 수집합니다.
   *
   * <p>수집된 URI는 {@code validEndpoints} 내부에 저장되며,
   * 스케줄러 등록 등 외부 요청에 대한 URI 유효성 검증 시 참조됩니다.
   *
   * <p>패턴은 {@code /foo}, {@code /bar/{id}} 형태의 URI 템플릿 문자열이며,
   * 실제 요청 URI와의 비교 로직은 별도로 구현되어야 합니다.
   */
  @PostConstruct
  public void init() {
    if (isInitialed) {
      return;
    }

    Map<RequestMappingInfo, ?> handlerMethods = handlerMapping.getHandlerMethods();

    // EXCLUDE_PREFIX로 동작하는 요소들은 거르고 등록
    for (RequestMappingInfo info : handlerMethods.keySet()) {
      for (String pattern : info.getPatternValues()) {
        if (!pattern.startsWith(EXCLUDE_PREFIX)) {
          validEndpoints.add(pattern);
        }
      }
    }
    isInitialed = true;
  }

  /**
   * 현재 등록된 컨트롤러 엔드포인트 목록을 반환합니다.
   *
   * @return 등록된 URI 목록
   */
  public List<String> getAllEndpoints() {
    return List.copyOf(new ArrayList<>(validEndpoints));
  }

  /**
   * 해당 URI가 실제 컨트롤러에 매핑된 URI인지 확인합니다.
   *
   * @param uri 요청 대상 URI
   * @return 매핑된 URI인지 여부
   */
  public boolean isValidEndpoint(String uri) {
    return validEndpoints.contains(uri);
  }

}
