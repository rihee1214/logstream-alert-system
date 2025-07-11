package com.rihee.alerting.loggingService.service;

import java.util.Map;

/**
 * {@code LogIngestService}는 수신된 로그 데이터를 구조화하고 저장소에 전달하기 위한 서비스 인터페이스입니다.
 *
 * <p>Kafka, REST API 등 외부에서 수신된 로그를 내부 표준 포맷에 맞게 가공하고,
 * 저장소에 기록하는 전 과정의 비즈니스 로직을 담당합니다.
 *
 * <p>이 인터페이스는 단순 저장 이상의 역할(예: 키 정규화, 필터링, 파싱 정책 적용 등)을 포함할 수 있으며,
 * 다양한 저장 전략 또는 검증 로직이 적용되는 중심 계층으로 설계되어야 합니다.
 *
 * <p>구체적인 구현체는 {@code LogIngestDao}와 같은 DAO를 활용하여 실제 저장을 수행하며,
 * 예외 발생 시 로깅 또는 알림 시스템과 연동될 수 있도록 설계됩니다.
 *
 * @author 리희
 * @since 1.0
 */
public interface LogIngestService {

  /**
   * 수신된 로그 데이터를 구조화하고 저장소에 기록합니다.
   *
   * <p>입력된 로그는 내부 정책에 따라 키 정규화(예: {@code call.uri → call_uri}) 및 필드 검증을 거친 후,
   * 저장소에 맞는 구조로 가공되어 저장됩니다.
   *
   * <p>입력 데이터는 Kafka 또는 외부 API 등 다양한 방식으로 수신될 수 있으며,
   * 처리 실패 시 예외를 발생시키기보다, 로그를 출력하고 무시하는 방식으로 설계하는 것이 일반적입니다.
   *
   * @param log 구조화되지 않은 원시 로그 데이터. {@code Map<String, Object>} 형태이며,
   *            로그 수집기나 외부 시스템에서 전달된 내용을 포함합니다.
   */
  void processingLog(Map<String, Object> log);
}
