package com.rihee.alerting.loggingService.core.pipeline.port.in;

import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessor;
import com.rihee.alerting.loggingService.core.plugin.LogCollectorPlugin;

/**
 * {@code LogCollector}는 로그 데이터를 외부 소스로부터 수집하는 책임을 가지는 추상 베이스 클래스입니다.
 *
 * <p>모든 수집기 구현체는 이 클래스를 상속해야 하며, 수집 대상에 따라 적절한 형태로
 * 로그를 가공한 후 {@code getLogDatas()} 메서드를 통해 수집된 로그 목록을 반환해야 합니다.
 *
 * <p>각 구현체는 내부에 정적 중첩 {@code Builder} 클래스를 정의하고,
 * 해당 클래스는 {@link Builder} 인터페이스를 반드시 구현해야 합니다.
 * 이 {@code Builder}는 설정 정보를 바탕으로 수집기 인스턴스를 생성하는 역할을 하며,
 * 외부에서 {@code public static Builder builder()} 메서드를 통해 접근 가능해야 합니다.
 *
 * <p>{@link com.rihee.alerting.loggingService.annotations.CollectorType} 어노테이션과 함께 사용되며,
 * 런타임 시 설정 기반으로 동적으로 수집기 인스턴스를 생성하는 데 사용됩니다.
 *
 * <p>이 추상 클래스를 기반으로 Kafka, HTTP 등 다양한 유형의 수집기를 구성할 수 있습니다.
 *
 * @see Builder
 * @see com.rihee.alerting.loggingService.annotations.CollectorType
 * @see LogCollectorPlugin
 */
public abstract class LogCollectorPort implements LogProcessor {


}
