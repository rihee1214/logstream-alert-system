/**
 * 이 패키지는 로그 수집 파이프라인에서 비즈니스 서비스와 로그 저장 서비스가
 * 공통으로 사용하는 로그 스키마 정의를 포함하고 있습니다.
 *
 * <p>주요 컴포넌트:</p>
 * <ul>
 *   <li>{@link com.rihee.alerting.common.constant.logging.StructuredLogFields}
 *   - 공통 필수 필드 정의</li>
 *   <li>{@link com.rihee.alerting.common.constant.logging.LogType}
 *   - 로그의 분류 타입</li>
 * </ul>
 *
 * <p>이 패키지에 포함된 {@code StructuredLogProperties}는 실제 로그 메시지에 포함되는 필드를 정의합니다.
 * </p>
 *
 * <p>해당 패키지의 모든 구성 요소는 비즈니스 서비스와 로깅 서비스 간의 **공통 스키마 공유**를 목적으로 하며,
 * 로그 메시지의 필드 구성 일관성을 보장하기 위해 정의되었습니다.
 * </p>
 */
package com.rihee.alerting.common.constant.logging;
