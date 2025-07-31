package com.rihee.alerting.loggingService.annotations;

import com.rihee.alerting.loggingService.persistence.LogPersistence;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @PersistenceType}은 로그 영속성 처리기(LogPersistence) 구현체를 식별하기 위한 어노테이션입니다.
 *
 * <p>이 어노테이션은 {@code com.rihee.alerting.loggingService.persistence.impl} 하위 패키지에 위치한
 * 클래스에만 적용되어야 하며, 해당 클래스는 반드시 {@link LogPersistence}
 * 인터페이스를 구현해야 합니다.
 *
 * <p>{@code value()}는 설정 파일에서 정의된 {@code persistence.type} 속성과 매칭되며,
 * 런타임 시 어떤 영속성 저장 방식을 사용할지 결정하는 **핵심 식별자(primary key 역할)**입니다.
 * 동일한 {@code value()}를 가지는 구현체가 여러 개 존재할 경우,
 * annotation processor에 의해 **컴파일 시 빌드가 실패**됩니다.
 *
 * <p><b>사용 조건:</b> 이 어노테이션이 적용된 클래스는 다음 구조를 따라야 합니다:
 * <ul>
 *   <li>{@code public static LogPersistence.Builder<?> builder()} 메서드를 반드시 정의해야 합니다.
 *       해당 메서드는 설정 기반으로 인스턴스를 생성하기 위한 진입점입니다.
 *   </li>
 *   <li>내부에 {@code LogPersistence.Builder} 인터페이스를 구현한 중첩 {@code Builder} 클래스를 포함해야 하며,
 *       설정 값(Map 기반)을 받아 영속성 저장소 인스턴스를 구성해야 합니다.
 *   </li>
 * </ul>
 *
 * <p>설계 강제성과 자동화된 구성 처리를 위해, annotation processor를 통한 정적 분석이 수행되며,
 * 요구 조건을 위반할 경우 빌드 시점에 오류를 발생시켜 시스템 일관성을 보장합니다.
 *
 * @see LogPersistence
 * @see LogPersistence.Builder
 * @see com.rihee.alerting.loggingService.persistence.LogPersistenceSpec
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistenceType {

  /**
   * 해당 영속성 구현체의 고유 식별자 (예: "postgres", "mongodb" 등).
   *
   * <p>이 값은 설정 파일에서 {@code persistence.type} 속성과 매칭되며,
   * 런타임 시 어떤 저장 방식 구현체를 선택할지를 결정합니다.
   * 이는 시스템 내 영속성 처리 방식을 구분하기 위한 **사실상의 Primary Key**로 동작합니다.
   *
   * <p><b>중복 금지:</b> 동일한 {@code value()}를 가지는 클래스가 여러 개 존재할 경우,
   * annotation processor가 빌드 오류를 발생시킵니다.
   *
   * @return 영속성 처리기의 식별자 (고유해야 함)
   */
  String value();
}

