package com.rihee.alerting.common.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * StructuredLogger 기반의 일관된 MDC 세팅을 자동으로 지원하는 RestController 전용 어노테이션.
 *
 * <p>
 * - Spring의 {@link org.springframework.web.bind.annotation.RestController} 기능을 그대로 포함한다.
 * - 별도 설정 없이 이 어노테이션만 붙이면, 요청이 들어올 때 StructuredLogger에 필요한 MDC 값이 자동 세팅된다.
 * </p>
 *
 * <h2>주요 목적</h2>
 * <ul>
 *     <li>요청 단위로 필요한 MDC 값을 자동 주입</li>
 *     <li>개발자가 별도 MDC 코드를 작성하지 않고 일관된 로그 구조를 유지</li>
 * </ul>
 *
 * <h2>사용 방법</h2>
 * <pre>{@code
 * @StructuredRestController
 * public class SomeApiController {
 *     // ...
 * }
 * }</pre>
 *
 * @see com.rihee.alerting.common.log.aspect.StructuredMdcAspect
 * @see org.springframework.web.bind.annotation.RestController
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
@Deprecated
public @interface StructuredRestController {
}
