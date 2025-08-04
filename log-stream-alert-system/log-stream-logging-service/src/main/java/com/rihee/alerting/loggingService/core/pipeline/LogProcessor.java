package com.rihee.alerting.loggingService.core.pipeline;

import com.rihee.alerting.loggingService.core.message.LogMessage;
import com.rihee.alerting.loggingService.core.runtime.LogWorker;
import java.util.Map;

/**
 * 로그 처리 파이프라인에서 각 단계별 처리기를 나타내는 최상위 인터페이스입니다.
 *
 * <p>이 인터페이스는 로그 메시지 {@link LogMessage}를 입력으로 받아 가공/검증/저장 등 다양한 형태로 처리한 후,
 * 동일하거나 수정된 로그 메시지를 반환하는 방식으로 구성된 처리 단계를 의미합니다.
 *
 * <p>파이프라인은 일반적으로 다음과 같은 단계로 구성됩니다:
 * <ul>
 *   <li>Collector: 외부 시스템(Kafka 등)으로부터 로그 수집</li>
 *   <li>Validator: 로그 메시지 구조, 필드 유효성 검증</li>
 *   <li>Persister: 로그를 영속 저장소(PostgreSQL 등)에 저장</li>
 * </ul>
 *
 * <p>각 단계는 이 {@code LogProcessor}를 구현하여 파이프라인 내 처리 단위로 동작하며,
 * {@code LoggingWorker} 내부에서 순차적으로 호출됩니다.
 *
 * @implSpec
 *      구현체는 순수하게 입력 로그 리스트를 처리하고, 출력 리스트로 결과를 반환해야 합니다.
 *      이때 입력 리스트를 직접 수정하지 않고 새로운 {@link LogMessage} 객체 리스트를 만들어 반환하는 것을 권장합니다
 *      (불변성 또는 명시적 복사를 통해 side-effect를 최소화).
 *      또한 모든 {@code LogProcessor} 구현체는 반드시 다음 특성을 지켜야 합니다:
 *      <ul>
 *          <li>스레드 세이프하지 않아도 됨 (worker 단일 스레드에서 동작)</li>
 *          <li>입력 메시지가 비어 있어도 안전하게 처리 가능해야 함</li>
 *          <li>필요시 내부 상태를 가지지 않고 순수 함수형 처리 권장</li>
 *      </ul>
 *
 * @see LogMessage
 * @see LogWorker
 * @see com.rihee.alerting.loggingService.collectors.LogCollector
 * @see com.rihee.alerting.loggingService.validators.LogValidator
 * @see com.rihee.alerting.loggingService.persistence.LogPersistence
 */
public interface LogProcessor {

  LogProcessingContext process(LogProcessingContext processingContext);


  interface Builder<T extends LogProcessor> {

    LogProcessor.Builder<T> withProperties(Map<String, String> setting);

    T build();
  }
}
