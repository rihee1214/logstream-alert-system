package com.rihee.alerting.common.constant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @LogPolicy}는 로그 필드에 대한 정책 정보를 정의하는 커스텀 어노테이션입니다.
 *
 * <p>이 어노테이션은 로그 시스템 내에서 각 필드가 가지는 역할과 중요도를 명시하며,
 * 로그 필드(Enum 등)에 부착하여 검증, 필터링, 알림 정책 등의 기준으로 활용됩니다.
 *
 * <p>일반적으로 {@code LogFieldKey} 인터페이스를 구현하는 Enum 클래스의 필드에 적용됩니다.
 *
 * <p>예시 사용:
 * <pre>{@code
 * @LogPolicy(isEssential = true, description = "로그 발생 시각을 나타냅니다.")
 * TIME_STAMP("timestamp"),
 * }</pre>
 *
 * @see com.rihee.alerting.common.constant.message.StructuredLogProperties
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogPolicy {

  /**
   * 해당 로그 필드가 필수 요소인지 여부를 나타냅니다.
   *
   * <p>true로 설정된 경우, 로그 생성 또는 검증 시 누락 여부를 검사하며,
   * 로깅/알림 시스템에서 필수 기준으로 간주됩니다.
   *
   * @return 필수 여부
   */
  boolean isEssential() default false;
  /**
   * 해당 로그 필드의 의미와 목적에 대한 설명입니다.
   *
   * <p>문서 자동 생성 또는 UI 기반 설정 화면에서 해당 필드의 용도를 표시할 때 활용됩니다.
   *
   * @return 필드 설명
   */
  String description() default "";
}
