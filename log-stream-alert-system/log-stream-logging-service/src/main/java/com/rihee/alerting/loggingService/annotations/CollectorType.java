package com.rihee.alerting.loggingService.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @CollectorType}은 로그 수집기(LogCollector) 구현체를 식별하기 위한 핵심 어노테이션입니다.
 *
 * <p>이 어노테이션은 {@code com.rihee.alerting.loggingService.collectors.impl} 하위 패키지에 위치한
 * 클래스에만 적용되어야 하며, 해당 클래스는 반드시 {@link com.rihee.alerting.loggingService.collectors.LogCollector}
 * 추상 클래스를 상속해야 합니다.
 *
 * <p>{@code value()}는 설정 파일에서 정의되는 {@code collector.type} 속성과 매칭되며,
 * **어떤 로그 수집기를 사용할지 결정하는 핵심 식별자(primary key 역할)**입니다.
 * 이 값은 시스템 전체에서 **중복이 절대 허용되지 않으며**, 동일한 {@code value()}를 갖는 수집기가 둘 이상 존재하면
 * annotation processor에 의해 **컴파일이 즉시 실패**됩니다.
 *
 * <p><b>사용 조건:</b> 이 어노테이션이 적용된 클래스는 반드시 다음의 구조를 따라야 합니다:
 * <ul>
 *   <li>{@code public static LogCollector.Builder<?> builder()} 메서드를 정의해야 합니다.
 *       이 메서드는 외부에서 수집기의 인스턴스를 생성하기 위한 진입점 역할을 합니다.
 *   </li>
 *   <li>내부에 {@code LogCollector.Builder} 인터페이스를 구현한 중첩 {@code Builder} 클래스를 포함해야 하며,
 *       이 빌더는 {@code Map<String, String>} 형태의 설정 값을 기반으로 수집기 인스턴스를 구성해야 합니다.
 *   </li>
 * </ul>
 *
 * <p>이 어노테이션은 런타임 설정 기반 구성과 아키텍처 일관성 유지를 위한 기반으로 활용되며,
 * 설계 강제와 시스템 안정성 확보를 위해 **annotation processor에 의해 엄격히 검증**됩니다.
 *
 * @see com.rihee.alerting.loggingService.collectors.LogCollector
 * @see com.rihee.alerting.loggingService.collectors.LogCollector.Builder
 * @see com.rihee.alerting.loggingService.collectors.LogCollectorSpec
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CollectorType {

  /**
   * 해당 수집기의 고유 식별자 (예: "kafka", "http" 등).
   *
   * <p>이 값은 설정 파일에서 {@code collector.type} 속성과 매칭되어,
   * 런타임에 어떤 수집기 구현체를 선택할지를 결정하는 데 사용됩니다.
   * 즉, 시스템 내 수집기 종류를 식별하는 **사실상의 Primary Key**입니다.
   *
   * <p><b>중복 금지:</b> 이 값이 중복되면 컴파일 시 {@code annotation processor}가 빌드를 실패시키므로,
   * 반드시 모든 구현체 간에 고유한 값을 사용해야 합니다.
   *
   * @return 수집기 타입 식별자 (고유해야 함)
   */
  String value();
}
