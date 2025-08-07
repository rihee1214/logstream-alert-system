/**
 * 이 패키지는 로그 수집 파이프라인에서 비즈니스 서비스와 로그 저장 서비스가
 * 공통으로 사용하는 로그 스키마 정의를 포함하고 있습니다.
 *
 * <p>주요 컴포넌트:</p>
 * <ul>
 *   <li>{@link com.rihee.alerting.common.constant.message.LogFieldKey}
 *   - 스키마 공통 인터페이스</li>
 *   <li>{@link com.rihee.alerting.common.constant.message.StructuredLogProperties}
 *   - 공통 필수 필드 정의</li>
 *   <li>{@link com.rihee.alerting.common.constant.message.CallCommonProperties}
 *   - 호출 관련 기본 필드 정의</li>
 *   <li>{@link com.rihee.alerting.common.constant.message.HttpCallProperties}
 *   - HTTP 호출 관련 필드 정의</li>
 *   <li>{@link com.rihee.alerting.common.constant.message.LogType}
 *   - 로그의 분류 타입</li>
 *   <li>{@link com.rihee.alerting.common.constant.message.CallType}
 *   - 호출 방식의 분류 타입</li>
 * </ul>
 *
 * <p>이 패키지에 포함된 {@code StructuredLogProperties}, {@code CallCommonProperties},
 * {@code HttpCallProperties} 등은 실제 로그 메시지에 포함되는 필드를 정의하며,
 * 모두 {@link com.rihee.alerting.common.constant.message.LogFieldKey} 인터페이스를 구현해야 합니다.
 * </p>
 *
 * <p>해당 패키지의 모든 구성 요소는 비즈니스 서비스와 로깅 서비스 간의 **공통 스키마 공유**를 목적으로 하며,
 * 로그 메시지의 필드 구성 일관성을 보장하기 위해 정의되었습니다.
 * </p>
 */
package com.rihee.alerting.common.constant.message;
