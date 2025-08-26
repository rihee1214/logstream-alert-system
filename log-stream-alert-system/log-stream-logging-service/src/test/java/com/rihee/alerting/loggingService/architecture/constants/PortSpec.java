package com.rihee.alerting.loggingService.architecture.constants;

import java.lang.annotation.Annotation;
import javax.annotation.processing.Processor;

public record PortSpec(String name,
                       Class<?> portInterface,
                       String adapterPackagePattern,
                       Class<? extends Annotation> adapterAnnotation,
                       Class<? extends Processor> adapterAnnotationProcessor,
                       Class<? extends Enum<?>> registryPathEnum) {

}
