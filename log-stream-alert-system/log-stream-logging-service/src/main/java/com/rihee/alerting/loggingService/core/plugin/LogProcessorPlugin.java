package com.rihee.alerting.loggingService.core.plugin;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;

/**
 * 로그 파이프라인에서 Collector, Validator, Persistence 등
 * 다양한 처리 단계를 플러그인 형태로 로딩하기 위한 공통 인터페이스입니다.
 *
 * <p>구현체는 각 단계별로 JSON 설정 파일
 * (예: {@code META-INF/logging/CollectorType.json},
 * {@code META-INF/logging/ValidatorType.json},
 * {@code META-INF/logging/PersistenceType.json})
 * 에 등록된 타입 식별자와 FQCN 매핑을 통해 동적으로 탐색·생성됩니다.
 *
 * <p>이 인터페이스는 두 가지 책임을 정의합니다:
 * <ul>
 *   <li>{@link #newProcessorInstance()} : 설정과 빌더를 기반으로
 *       새로운 {@link LogProcessorPort} 인스턴스를 생성</li>
 *   <li>{@link #getProcessorType()} : 플러그인의 타입 식별자를 반환
 *       (예: {@code "kafka"}, {@code "postgres"}, {@code "schema"})</li>
 * </ul>
 *
 * <p>즉, {@code LogProcessorPlugin}은 “플러그인 로더 + 팩토리”의 역할을 수행하며,
 * 런타임에서 특정 파이프라인 단계를 유연하게 교체하거나 확장할 수 있도록 설계되었습니다.
 *
 * @see LogProcessorPort
 * @see LogCollectorPlugin
 * @see LogValidatorPlugin
 * @see LogPersistencePlugin
 */
public interface LogProcessorPlugin {

  /**
   * 새로운 {@link LogProcessorPort} 인스턴스를 생성합니다.
   *
   * <p>내부적으로는 각 플러그인 구현체가 보관하고 있는
   * {@link LogProcessorPort.Builder}를 통해 인스턴스를 생성합니다.
   *
   * @return 새 {@link LogProcessorPort} 인스턴스
   */
  LogProcessorPort newProcessorInstance();

  /**
   * 현재 플러그인의 타입 식별자를 반환합니다.
   *
   * <p>이 값은 설정 파일(JSON)에 정의된 키와 동일하며,
   * 플러그인 종류를 구분하는 기준으로 사용됩니다.
   *
   * @return 플러그인 타입 문자열
   */
  String getProcessorType();
}

