package com.rihee.alerting.loggingService.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @CollectorType}은 로그 수집기(LogCollector) 구현체를 식별하기 위한 어노테이션입니다.
 *
 * <p>이 어노테이션은 {@code com.rihee.alerting.loggingService.collectors.impl} 하위 패키지에 위치한
 * 클래스에만 적용되어야 하며, 해당 클래스는 반드시 {@link com.rihee.alerting.loggingService.collectors.LogCollector}
 * 추상 클래스를 상속해야 합니다.
 *
 * <p>{@code value()}는 설정 파일의 {@code collector.type} 속성과 매칭되며, 런타임 시 알맞은 수집기 클래스를
 * 결정하는 기준으로 사용됩니다.
 *
 * <p><b>사용 조건:</b> 이 어노테이션이 적용된 클래스는 반드시 다음의 구조를 따라야 합니다:
 *
 * <ul>
 *   <li>{@code public static LogCollector.Builder<?> builder()} 메서드를 정의해야 합니다.
 *       이는 외부에서 접근 가능한 빌더 팩토리 메서드로, 수집기 인스턴스를 생성할 수 있어야 합니다.
 *   </li>
 *   <li>내부에 {@code LogCollector.Builder} 인터페이스를 구현한 중첩 {@code Builder} 클래스를 포함해야 하며,
 *       해당 빌더는 설정 정보(Map 기반)를 받아 실제 수집기 인스턴스를 구성해야 합니다.
 *   </li>
 * </ul>
 *
 * <p>동일한 {@code value()}를 가지는 클래스가 둘 이상 존재할 경우,
 * annotation processor에 의해 빌드가 중단됩니다.
 *
 * @see com.rihee.alerting.loggingService.collectors.LogCollector
 * @see com.rihee.alerting.loggingService.collectors.LogCollector.Builder
 * @see com.rihee.alerting.loggingService.collectors.LogCollectorSpec
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CollectorType {

  /**
   * 해당 수집기의 식별자 (예: "kafka", "http" 등).
   *
   * <p>설정 파일에서 {@code collector.type} 값과 매칭되며, 런타임 시 알맞은 수집기 클래스를 결정하는 기준으로 사용됩니다.
   *
   * @return 수집기 타입 식별자
   */
  String value();
}
