package com.rihee.alerting.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * {@code @StructuredPutMapping}은 {@link org.springframework.web.bind.annotation.PutMapping}의
 * 대체 어노테이션으로, 구조화된 로깅과 추적을 위해 {@code spanLabel} 속성을 필수로 지정하는 PUT 요청 매핑 어노테이션입니다.
 *
 * <p>기본적으로 {@link RequestMapping}의 {@code method = RequestMethod.PUT} 동작을 포함하며
 * 추가적으로 요청을 구분하고 로그 추적을 위해 사용할 {@code spanLabel} 속성을 반드시 명시해야 합니다.</p>
 *
 * <p>이는 {@code spanId} 생성 규칙에 활용되며, 서비스 호출 경로를 명확히 추적하는 데 사용됩니다.</p>
 *
 * <h2>사용 예:</h2>
 * <pre>{@code
 * @StructuredPutMapping(
 *     spanLabel = "joinUser",
 *     value = "/join"
 * )
 * }</pre>
 *
 * <p>{@code @PutMapping}, {@code @RequestMapping(method = RequestMethod.PUT)}의 사용은 금지되며,
 * 모든 PUT 요청은 반드시 {@code @StructuredPutMapping}을 통해 정의되어야 합니다. (Checkstyle 정책 적용)</p>
 *
 * <p><b>NOTE:</b> 메타 어노테이션(하위 애노테이션)으로 감싸 사용하지 마십시오.
 * 반드시 이 어노테이션을 직접 사용해야 합니다.</p>
 *
 * @see org.springframework.web.bind.annotation.PutMapping
 * @see RequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.PUT)
public @interface StructuredPutMapping {

  /**
   * 요청에 대한 명확한 식별자 역할을 하는 {@code spanLabel} 속성(spanId 지정에 필요).
   *
   * <p>로깅 및 추적용으로 사용되며, 반드시 명시되어야 합니다.</p>
   *
   * <p><b>주의:</b> 이 속성은 반드시 사용자가 명시해야 하며, 누락 시 컴파일 오류는 발생하지 않지만 런타임에서 로깅 정보가 누락됩니다.</p>
   */
  String spanLabel();

  /**
   * {@link RequestMapping#value}
   *
   * <p>URL 경로 매핑.</p>
   *
   * <p>예: "/users", "/api/items"</p>
   *
   * <p><b>주의:</b> 이 속성은 반드시 사용자가 명시해야 하며, 누락 시 컴파일 오류는 발생하지 않지만 런타임에서 정보가 누락됩니다.</p>
   */
  @AliasFor(annotation = RequestMapping.class, attribute = "value")
  String[] value() default {};

  /**
   * {@link RequestMapping#consumes}
   * 소비 가능한 MIME 타입.<br>
   * 예: "application/json"
   */
  @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
  String[] consumes() default {};

  /**
   * {@link RequestMapping#produces}
   * 생성 가능한 MIME 타입.<br>
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
