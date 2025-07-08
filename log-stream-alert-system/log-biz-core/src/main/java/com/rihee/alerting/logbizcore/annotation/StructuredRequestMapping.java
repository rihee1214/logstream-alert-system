package com.rihee.alerting.logbizcore.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * {@code @StructuredRequestMapping}은 {@link org.springframework.web.bind.annotation.RequestMapping}
 * 의 기능을 확장하여 커스텀 로깅 및 추적 처리를 위한 spanLabel 속성을 강제하는 구조화된 요청 매핑 어노테이션입니다.
 *
 * <p>기본적으로 Spring MVC의 {@code @RequestMapping} 속성을 위임받아 동일하게 사용되며,
 * 추가로 {@code spanLabel} 속성을 반드시 지정해야 하며, 이는 서비스 추적 및 로깅(spanId 구성) 시 사용됩니다.</p>
 *
 * <h2>예시 사용법:</h2>
 * <pre>{@code
 * @StructuredRequestMapping(
 *     spanLabel = "get-users",
 *     value = "/users",
 *     method = RequestMethod.GET
 * )
 * }</pre>
 *
 * <p>이 어노테이션은 모든 HTTP 메서드(@GetMapping, @PostMapping 등)를 감싸는 메타 어노테이션으로도 사용될 수 있으며,
 * 로깅, 추적 ID 할당(MDC 활용)을 위한 기반으로 활용됩니다.</p>
 *
 * <p><b>NOTE:</b> 메타 어노테이션(하위 애노테이션)으로 감싸 사용하지 마십시오.
 * 반드시 이 어노테이션을 직접 사용해야 합니다.</p>
 *
 * @see org.springframework.web.bind.annotation.RequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping
public @interface StructuredRequestMapping {

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
   * {@link RequestMapping#method}
   * 허용할 HTTP 메서드.<br>
   * 예: GET, POST 등.
   */
  @AliasFor(annotation = RequestMapping.class, attribute = "method")
  RequestMethod[] method() default {};

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
