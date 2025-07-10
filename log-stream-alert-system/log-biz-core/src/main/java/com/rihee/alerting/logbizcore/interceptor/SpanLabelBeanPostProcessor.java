package com.rihee.alerting.logbizcore.interceptor;

import com.rihee.alerting.logbizcore.annotation.StructuredDeleteMapping;
import com.rihee.alerting.logbizcore.annotation.StructuredGetMapping;
import com.rihee.alerting.logbizcore.annotation.StructuredPatchMapping;
import com.rihee.alerting.logbizcore.annotation.StructuredPostMapping;
import com.rihee.alerting.logbizcore.annotation.StructuredPutMapping;
import com.rihee.alerting.logbizcore.annotation.StructuredRequestMapping;
import io.micrometer.common.lang.NonNullApi;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * {@code SpanLabelBeanPostProcessor}는 Spring의 {@link BeanPostProcessor}를 구현하여
 * 애플리케이션의 모든 Bean 초기화 이후, 특정 사용자 정의 HTTP Mapping 어노테이션이 붙은 메서드를 검사하고,
 * 해당 어노테이션의 {@code spanLabel} 값을 {@link SpanLabelRegistry}에 등록합니다.
 *
 * <p>{@link StructuredRequestMapping}, {@link StructuredGetMapping}, {@link StructuredPostMapping},
 * {@link StructuredDeleteMapping}, {@link StructuredPatchMapping}, {@link StructuredPutMapping}
 * 어노테이션을 대상으로 분석하며, 이 어노테이션들은 공통적으로 {@code spanLabel()} 속성을 가지고 있습니다.
 * 추가될 어노테이션도 {@code spanLabel()} 속성을 가지고 있어야만 합니다.
 * </p>
 *
 * <p>AOP 프록시 객체가 전달되는 경우를 대비하여, 실제 대상 클래스는 {@link AopUtils#getTargetClass(Object)}를 통해 가져옵니다.
 * </p>
 *
 * @see SpanLabelRegistry
 */
@NonNullApi
public class SpanLabelBeanPostProcessor implements BeanPostProcessor {

  /**
   * 분석 대상이 되는 사용자 정의 HTTP Mapping 어노테이션 클래스 목록입니다.
   * 각 어노테이션은 {@code spanLabel()} 메서드를 포함해야 합니다.
   */
  private static final Set<Class<? extends Annotation>> targetAnnotations
                                                            = new HashSet<>(List.of(
                                                                    StructuredRequestMapping.class,
                                                                    StructuredGetMapping.class,
                                                                    StructuredPostMapping.class,
                                                                    StructuredDeleteMapping.class,
                                                                    StructuredPatchMapping.class,
                                                                    StructuredPutMapping.class
                                                            ));

  private final SpanLabelRegistry registry;

  /**
   * SpanLabelBeanPostProcessor 생성자입니다.
   *
   * @param registry {@link SpanLabelRegistry}를 주입받아 spanLabel 정보를 등록할 때 사용됩니다.
   */
  public SpanLabelBeanPostProcessor(SpanLabelRegistry registry) {
    this.registry = registry;
  }

  /**
   * Bean 초기화 이후 실행되는 후처리 로직입니다.
   *
   * <p>컨트롤러 클래스의 모든 메서드를 탐색하여, 구조화된 로깅 어노테이션이 붙어 있는 경우
   * 해당 어노테이션의 {@code spanLabel()} 값을 추출하여 {@link SpanLabelRegistry}에 등록합니다.
   * </p>
   *
   * @param bean     초기화가 완료된 Bean 객체
   * @param beanName Bean 이름
   * @return 원래의 Bean 객체(수정 없음)
   * @throws BeansException Spring 초기화 중 예외 발생 시
   */
  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> targetClass = AopUtils.getTargetClass(bean);

    for (Method method : targetClass.getDeclaredMethods()) {
      registerSpanLabelIfPresent(method);
    }
    return bean;
  }

  /**
   * 지정된 메서드에 구조화된 로깅 어노테이션이 존재할 경우,
   * 해당 어노테이션의 {@code spanLabel()} 값을 추출하여 {@link SpanLabelRegistry}에 등록합니다.
   *
   * <p>이 메서드는 {@code targetAnnotations}에 명시된 정확한 어노테이션 타입만 허용하며,</p>
   *
   * <p>메타 어노테이션(하위 어노테이션)을 통한 간접 선언은 인식되지 않습니다.</p>
   *
   * <p>{@code spanLabel} 속성은 필수이며, 비어 있을 경우 등록되지 않습니다.</p>
   *
   * <p>한 메서드에 여러 어노테이션이 있을 경우 첫 번째 유효한 {@code spanLabel}만 등록됩니다.</p>
   *
   * @param method 검사 대상 메서드
   * @throws IllegalStateException spanLabel 추출 중 리플렉션 오류 발생 시
   */
  private void registerSpanLabelIfPresent(Method method) {
    for (Annotation methodAnnotation : method.getDeclaredAnnotations()) {
      if (targetAnnotations.contains(methodAnnotation.annotationType())) {
        try {
          Method labelMethod = methodAnnotation.annotationType().getMethod("spanLabel");
          Object value = labelMethod.invoke(methodAnnotation);
          if (value instanceof String label && !label.isEmpty()) {
            registry.register(method, label);
            break;
          }
        } catch (Exception e) {
          throw new IllegalStateException("Failed to extract spanLabel from annotation", e);
        }
      }
    }
  }

}
