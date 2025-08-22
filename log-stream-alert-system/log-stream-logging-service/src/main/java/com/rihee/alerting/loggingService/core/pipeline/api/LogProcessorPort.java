package com.rihee.alerting.loggingService.core.pipeline.api;

import com.rihee.alerting.loggingService.core.model.LogMessage;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.util.Map;

/**
 * 로그 메시지를 처리하는 모든 컴포넌트가 구현해야 하는 공통 인터페이스입니다.
 *
 * <p>Kafka, File, HTTP 등 다양한 수집 방식으로 전달된 로그를 처리하는 각 단계를
 * 독립적인 모듈로 분리하고, 그 처리 흐름을 정의하기 위해 사용됩니다.
 *
 * <p>일반적으로 다음과 같은 처리 단계를 담당하는 구현체들이 존재할 수 있습니다:
 * <ul>
 *   <li>구문 및 구조 검증 (Syntax / Schema validation)</li>
 *   <li>필드 정제 및 추가 가공 (Enrichment / Normalization)</li>
 *   <li>비즈니스 검증 (Business Rule Validation)</li>
 *   <li>영속화 처리 (DB / Kafka / Storage 등)</li>
 * </ul>
 *
 * @apiNote
 *     구현체는 불변성을 갖는 {@link LogProcessingContext}를 입력받아 처리한 후,
 *     새로운 {@code LogProcessingContext} 인스턴스를 반환해야 합니다.
 *     반환값은 절대 {@code null}이 되어서는 안 되며, 처리 실패 시에도 적절한 상태를 담은 컨텍스트를 반환해야 합니다.
 *     <br>
 *     <b>스레드 안정성(Thread-Safety)은 구현체에 따라 보장되지 않을 수 있으므로,
 *     프레임워크 측에서 단일 스레드로 실행됨을 전제로 합니다.</b>
 *
 * @see LogProcessingContext
 * @see LogMessage
 */
public interface LogProcessorPort {

  /**
   * 주어진 로그 처리 컨텍스트를 기반으로 단일 단계의 처리를 수행합니다.
   *
   * @param processingContext 현재 처리 대상이 되는 로그 메시지 및 상태 정보
   * @return 로그 처리 결과 및 흐름 제어 정보를 담은 {@link ProcessResult} 객체.
   *         컨텍스트 상태뿐 아니라, 처리 지속 여부 및 커밋 가능 여부를 포함합니다.
   *
   * @apiNote
   *     반환되는 {@link ProcessResult}는 로그 처리의 성공/실패뿐 아니라,
   *     다음 단계로의 진행 여부, 커밋 가능 여부를 함께 전달하기 위한 목적입니다.
   *     그 구체적인 의미와 구조는 {@link ProcessResult} 문서를 참고하십시오.
   */
  ProcessResult process(LogProcessingContext processingContext);

  /**
   * {@link LogProcessorPort} 구현체 생성을 위한 빌더 인터페이스입니다.
   *
   * @param <T> 생성 대상 LogProcessor의 구체 타입
   *
   * @apiNote
   *     구현체는 반드시 무상태(stateless)로 생성되어야 하며,
   *     빌더가 내부적으로 상태를 갖고 있어도 동시성 처리는 책임지지 않습니다.
   */
  interface Builder<T extends LogProcessorPort> {

    /**
     * 설정 정보를 빌더에 주입합니다.
     *
     * @param setting 키-값 쌍 형태의 설정 값
     * @return 빌더 인스턴스 자체 (체이닝 지원)
     *
     * @apiNote
     *     설정은 명세된 키에 따라 정확히 해석되어야 하며,
     *     잘못된 설정이 들어올 경우 {@code build()} 호출 시점에 예외를 발생시켜야 합니다.
     */
    LogProcessorPort.Builder<T> withProperties(Map<String, String> setting);

    /**
     * 빌더에 설정된 정보로 {@link LogProcessorPort} 인스턴스를 생성합니다.
     *
     * @return 구성된 LogProcessor 구현체
     *
     * @throws IllegalStateException 필수 설정이 누락된 경우
     *
     * @apiNote
     *     이 메서드는 반드시 새로운 인스턴스를 생성하거나,
     *     재사용 가능한 안전한 싱글톤을 반환해야 합니다.
     */
    T build();
  }
}
