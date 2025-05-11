package com.rihee.alerting.common.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code SpanLabelRegistry}는 메서드 시그니처와 해당 메서드에 선언된 spanLabel 값을 매핑하여 저장하는 레지스트리입니다.
 *
 * <p>실제 등록 동작은 {@link com.rihee.alerting.common.interceptor.SpanLabelBeanPostProcessor}에 의해 수행되며,
 * 이 클래스는 단순히 spanLabel 조회와 등록, 제거 기능만 제공합니다.
 * </p>
 *
 * <p>메서드 식별을 위해 {@code 클래스명#메서드명[파라미터타입]} 형식의 고유 시그니처를 키로 사용합니다.
 * </p>
 *
 * <p><b>스레드 안전</b>: 내부적으로 {@link ConcurrentHashMap}을 사용하여 동시 접근에 안전합니다.</p>
 *
 * @see com.rihee.alerting.common.interceptor.SpanLabelBeanPostProcessor
 * @see DefaultStructuredLogInterceptor
 */
public class SpanLabelRegistry {

  // 각 method의 Method Full Qualified Name와 Annotation에 들어있는 spanLabel을 매핑시켜주는 Map
  private final Map<String, String> methodToLabel = new ConcurrentHashMap<>();

  /**
   * 메서드의 고유 시그니처를 키로 하여 spanLabel 값을 저장합니다.
   *
   * @param method spanLabel이 부여된 대상 메서드
   * @param label  해당 메서드에 지정된 spanLabel 값
   */
  public void register(Method method, String label) {
    methodToLabel.put(makeFullSignatureByMethod(method), label);
  }

  /**
   * 메서드의 고유 시그니처를 키로 하여 저장된 spanLabel 값을 제거합니다.
   *
   * @param method 등록을 해제할 대상 메서드
   */
  public void unregister(Method method) {
    methodToLabel.remove(makeFullSignatureByMethod(method));
  }

  /**
   * 주어진 메서드에 해당하는 spanLabel 값을 조회합니다.
   *
   * @param method spanLabel 조회 대상 메서드
   * @return Optional로 래핑된 spanLabel 값. 등록되지 않은 경우 빈 Optional 반환
   */
  public Optional<String> findLabel(Method method) {
    return Optional.ofNullable(methodToLabel.get(makeFullSignatureByMethod(method)));
  }

  /**
   * 주어진 메서드의 전체 시그니처를 생성합니다.
   * 형식: {@code 클래스명#메서드명[파라미터타입, ...]}
   *
   * @param method 대상 메서드
   * @return 메서드의 유일한 전체 시그니처 문자열
   */
  private String makeFullSignatureByMethod(Method method) {
    return method.getDeclaringClass().getName() + "#" + method.getName()
          + Arrays.toString(method.getParameterTypes());
  }
}
