package com.rihee.alerting.loggingService.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ValidatorType}은 로그 유효성 검사기(LogValidator) 구현체를 식별하기 위한 어노테이션입니다.
 *
 * <p>이 어노테이션은 {@code com.rihee.alerting.loggingService.validators.impl} 하위 패키지에 위치한
 * 클래스에만 적용되어야 하며, 해당 클래스는 반드시 LogValidatorPort로써 동작해야 합니다.
 *
 * <p>{@code value()}는 설정 파일에서 정의된 {@code validator.type} 속성과 매칭되며,
 * 런타임 시 어떤 유효성 검사기 구현체를 사용할지 결정하는 **핵심 식별자(사실상의 Primary Key)**로 작동합니다.
 * 동일한 {@code value()}를 가지는 구현체가 둘 이상 존재할 경우, annotation processor에 의해
 * **컴파일 시점에 빌드 오류가 발생**합니다.
 *
 * <p><b>사용 조건:</b> 이 어노테이션이 적용된 클래스는 다음 구조를 반드시 따라야 합니다:
 * <ul>
 *   <li>{@code public static LogValidator.Builder<?> builder()} 메서드를 정의해야 합니다.
 *       이 메서드는 외부에서 설정 기반으로 인스턴스를 생성할 수 있도록 하는 진입점입니다.
 *   </li>
 *   <li>내부에 {@code LogValidator.Builder} 인터페이스를 구현한 중첩 {@code Builder} 클래스를 포함해야 하며,
 *       설정 정보(Map 기반)를 받아 실제 유효성 검사기 인스턴스를 생성할 수 있어야 합니다.
 *   </li>
 * </ul>
 *
 * <p>시스템 일관성과 아키텍처 강제를 위해, annotation processor를 통한 정적 분석이 수행되며,
 * 요구 조건 위반 시 컴파일이 중단됩니다. 이를 통해 런타임 오류를 사전에 방지하고,
 * 표준화된 로그 유효성 검사기 구성을 보장할 수 있습니다.
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ValidatorType {

  /**
   * 해당 유효성 검사기 구현체의 고유 식별자 (예: "basic", "schema", "strict" 등).
   *
   * <p>이 값은 설정 파일의 {@code validator.type} 항목과 매칭되며,
   * 어떤 유효성 검사 로직을 사용할지를 결정하는 기준입니다.
   * 이 값은 시스템 내 유효성 검사 전략을 구분하는 **사실상의 Primary Key** 역할을 하며, 반드시 고유해야 합니다.
   *
   * <p><b>중복 금지:</b> 동일한 {@code value()}를 가지는 클래스가 둘 이상 존재할 경우,
   * annotation processor에 의해 빌드 오류가 발생합니다.
   *
   * @return 유효성 검사기의 식별자 (고유해야 함)
   */
  String value();
}
