package com.rihee.alerting.logbizcore.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code SpanLabelRegistry}는 메서드 시그니처와 해당 메서드에 선언된 {@code spanLabel} 값을 매핑하여 저장하는 스레드 안전 레지스트리입니다.
 *
 * <p>이 레지스트리는 주로 {@link SpanLabelBeanPostProcessor}를 통해
 * 초기화되며, {@link StructuredLogInterceptor}가 요청을 처리할 때, 메서드 단위로 지정된 spanLabel을 추출하는 데 사용됩니다.</p>
 *
 * <p>메서드 시그니처는 {@code 클래스명#메서드명[파라미터타입, ...]} 형식으로 고유하게 구성되며,
 * 해당 키를 기반으로 {@code spanLabel}을 등록, 조회 및 제거할 수 있습니다.</p>
 *
 * <p><b>시스템 정책:</b> 추적 로그의 정확성 보장을 위해 각 컨트롤러 메서드의 spanLabel 설정은 필수이며,
 * 해당 정보는 StructuredLogInterceptor를 통한 spanId 생성 정책의 핵심 요소로 사용됩니다.</p>
 *
 * <p><b>스레드 안전:</b> 내부적으로 {@link ConcurrentHashMap}을 사용하여 동시 접근에 안전합니다.</p>
 *
 * @see SpanLabelBeanPostProcessor
 * @see StructuredLogInterceptor
 */
public class SpanLabelRegistry {

  // 각 method의 Method Full Qualified Name와 Annotation에 들어있는 spanLabel을 매핑시켜주는 Map
  private final Map<String, String> methodToLabel = new ConcurrentHashMap<>();

  /**
   * 메서드의 고유 시그니처를 키로 하여 {@code spanLabel} 값을 등록합니다.
   *
   * <p>일반적으로 {@link SpanLabelBeanPostProcessor}에서 호출되며,
   * 컨트롤러 메서드에 정의된 {@code @StructuredRequestMapping(spanLabel="...")} 등의 어노테이션 정보를 기반으로 합니다.</p>
   *
   * @param method spanLabel이 부여된 대상 메서드
   * @param label  해당 메서드에 지정된 spanLabel 값. {@code null} 또는 빈 문자열은 허용되지 않습니다.
   */
  public void register(Method method, String label) {
    methodToLabel.put(makeFullSignatureByMethod(method), label);
  }

  /**
   * 메서드의 고유 시그니처를 키로 하여 등록된 {@code spanLabel} 값을 제거합니다.
   *
   * <p>일반적으로 Spring Bean의 소멸이나 재등록 시점에 호출됩니다.</p>
   *
   * @param method 등록을 해제할 대상 메서드
   */
  public void unregister(Method method) {
    methodToLabel.remove(makeFullSignatureByMethod(method));
  }

  /**
   * 주어진 메서드에 해당하는 {@code spanLabel} 값을 조회합니다.
   *
   * @param method spanLabel 조회 대상 메서드
   * @return Optional로 래핑된 spanLabel 값. 등록되지 않은 경우 {@code Optional.empty()} 반환
   */
  public Optional<String> findLabel(Method method) {
    return Optional.ofNullable(methodToLabel.get(makeFullSignatureByMethod(method)));
  }

  /**
   * 주어진 메서드의 전체 시그니처를 생성합니다.
   *
   * <p>형식: {@code 클래스명#메서드명[파라미터타입, ...]}<br>
   * 예시: {@code com.example.MyController#findUserById(class java.lang.String)}</p>
   *
   * @param method 대상 메서드
   * @return 메서드의 유일한 전체 시그니처 문자열
   */
  private String makeFullSignatureByMethod(Method method) {
    return method.getDeclaringClass().getName() + "#" + method.getName()
          + Arrays.toString(method.getParameterTypes());
  }
}
