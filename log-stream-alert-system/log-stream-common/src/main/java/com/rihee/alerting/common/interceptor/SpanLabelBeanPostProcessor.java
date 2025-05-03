package com.rihee.alerting.common.interceptor;

import com.rihee.alerting.common.annotation.*;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code SpanLabelBeanPostProcessor}는 Spring의 {@link BeanPostProcessor}를 구현하여
 * 애플리케이션의 모든 Bean 초기화 이후, 특정 사용자 정의 HTTP Mapping 어노테이션이 붙은 메서드를 검사하고,
 * 해당 어노테이션의 {@code spanLabel} 값을 {@link SpanLabelRegistry}에 등록합니다.
 *
 * <p>
 * {@link StructuredRequestMapping}, {@link StructuredGetMapping}, {@link StructuredPostMapping},
 * {@link StructuredDeleteMapping}, {@link StructuredPatchMapping}, {@link StructuredPutMapping}
 * 어노테이션을 대상으로 분석하며, 이 어노테이션들은 공통적으로 {@code spanLabel()} 속성을 가지고 있습니다.
 * 추가될 어노테이션도 {@code spanLabel()} 속성을 가지고 있어야만 합니다.
 * </p>
 *
 * <p>
 * AOP 프록시 객체가 전달되는 경우를 대비하여, 실제 대상 클래스는 {@link AopUtils#getTargetClass(Object)}를 통해 가져옵니다.
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
     * 클래스의 모든 메서드를 탐색하여 대상 어노테이션이 존재하는 경우 해당 어노테이션의 {@code spanLabel()} 값을 추출하고,
     * {@link SpanLabelRegistry}에 해당 메서드와 함께 등록합니다.
     *
     * <p>메서드에 여러 어노테이션이 존재하더라도, 첫 번째 유효한 spanLabel만 등록됩니다.</p>
     *
     * @param bean      초기화된 Bean 객체
     * @param beanName  Bean의 이름
     * @return 원래의 Bean 객체 (변형 없음)
     * @throws BeansException Spring Bean 처리 중 예외가 발생한 경우
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        spanLabelFinder : for (Method method : targetClass.getDeclaredMethods()){
            for (Annotation methodAnnotation : method.getAnnotations()) {
                for (Class<? extends Annotation> annotationClass : targetAnnotations) {
                    Annotation annotation = AnnotationUtils.findAnnotation(methodAnnotation.annotationType(), annotationClass);
                    if (annotation != null) {
                        try {
                            Method labelMethod = annotation.annotationType().getMethod("spanLabel");
                            Object value = labelMethod.invoke(methodAnnotation);
                            if (value instanceof String label && !label.isEmpty()) {
                                registry.register(method, label);
                                break spanLabelFinder;
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to extract spanLabel from annotation", e);
                        }
                    }
                }
            }
        }
        return bean;
    }

}
