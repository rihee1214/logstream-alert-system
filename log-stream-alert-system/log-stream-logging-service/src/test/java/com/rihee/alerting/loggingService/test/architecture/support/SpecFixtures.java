package com.rihee.alerting.loggingService.test.architecture.support;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import com.rihee.alerting.loggingService.test.architecture.constants.PortSpec;
import com.rihee.alerting.loggingService.core.pipeline.port.in.LogCollectorPort;
import com.rihee.alerting.loggingService.core.pipeline.port.out.LogPersistencePort;
import com.rihee.alerting.loggingService.core.pipeline.port.rule.LogValidatorPort;
import com.rihee.alerting.loggingService.core.plugin.LogCollectorPlugin;
import com.rihee.alerting.loggingService.core.plugin.LogPersistencePlugin;
import com.rihee.alerting.loggingService.core.plugin.LogValidatorPlugin;
import com.rihee.alerting.loggingService.tools.annotationprocessor.CollectorTypeProcessor;
import com.rihee.alerting.loggingService.tools.annotationprocessor.PersistenceTypeProcessor;
import com.rihee.alerting.loggingService.tools.annotationprocessor.ValidatorTypeProcessor;
import com.rihee.alerting.loggingService.tools.constants.ProcessorRegistryPaths;
import java.util.stream.Stream;

public final class SpecFixtures {

  private SpecFixtures() {}

  public static Stream<PortSpec> specs() {
    return Stream.of(
        new PortSpec(
            "Collector",
            LogCollectorPort.class,
            "..adapter.in.collector..",
            LogCollectorPlugin.class,
            CollectorType.class,
            CollectorTypeProcessor.class,
            ProcessorRegistryPaths.COLLECTOR.getDeclaringClass()
        ),
        new PortSpec(
            "Validator",
            LogValidatorPort.class,
            "..adapter.rule.validator..",
            LogValidatorPlugin.class,
            ValidatorType.class,
            ValidatorTypeProcessor.class,
            ProcessorRegistryPaths.VALIDATOR.getDeclaringClass()
        ),
        new PortSpec(
            "Persistence",
            LogPersistencePort.class,
            "..adapter.out.persistence..",
            LogPersistencePlugin.class,
            PersistenceType.class,
            PersistenceTypeProcessor.class,
            ProcessorRegistryPaths.PERSISTENCE.getDeclaringClass()
        )
    );
  }
}
