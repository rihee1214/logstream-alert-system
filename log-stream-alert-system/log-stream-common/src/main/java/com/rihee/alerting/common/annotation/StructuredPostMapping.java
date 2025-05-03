package com.rihee.alerting.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

/**
 * {@code @StructuredPostMapping}은 {@link org.springframework.web.bind.annotation.PostMapping}의 대체 어노테이션으로,
 * 구조화된 로깅과 추적을 위해 {@code spanLabel} 속성을 필수로 지정하는 POST 요청 매핑 어노테이션입니다.
 *
 * <p>기본적으로 {@link RequestMapping}의 {@code method = RequestMethod.POST} 동작을 포함하며,</p>
 * <p>추가적으로 요청을 구분하고 로그 추적을 위해 사용할 {@code spanLabel} 속성을 반드시 명시해야 합니다.</p>
 *
 * <p>이는 {@code spanId} 생성 규칙에 활용되며, 서비스 호출 경로를 명확히 추적하는 데 사용됩니다.</p>
 *
 * <h2>사용 예:</h2>
 * <pre>{@code
 * @StructuredPostMapping(
 *     spanLabel = "login",
 *     value = "/login"
 * )
 * }</pre>
 *
 * <p>※ 단일 POST 요청은 반드시 이 어노테이션을 사용해야 하며,
 * 기존 {@code @PostMapping}, {@code @RequestMapping(method = RequestMethod.Post)}은 사용할 수 없습니다.
 * (Checkstyle 정책으로 금지)</p>
 *
 * @see org.springframework.web.bind.annotation.PostMapping
 * @see RequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.POST)
public @interface StructuredPostMapping {
    /**
     * 요청에 대한 명확한 식별자 역할을 하는 {@code spanLabel} 속성. (spanId 지정에 필요)
     * 로깅 및 추적용으로 사용되며, 반드시 명시되어야 합니다.
     */
    String spanLabel();

    /**
     * {@link RequestMapping#value}
     * URL 경로 매핑.
     * 예: "/users", "/api/items"
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
    /**
     * {@link RequestMapping#consumes}
     * 소비 가능한 MIME 타입.
     * 예: "application/json"
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
    String[] consumes() default {};
    /**
     * {@link RequestMapping#produces}
     * 생성 가능한 MIME 타입.
     * 예: "application/json"
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "produces")
    String[] produces() default {};
    /**
     * {@link RequestMapping#headers}
     * 요청 헤더 조건.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    String[] headers() default {};
    /**
     * {@link RequestMapping#params}
     * 요청 파라미터 조건.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "params")
    String[] params() default {};
}
