package com.rihee.alerting.loggingService.architecture.constants;

import java.lang.annotation.Annotation;
import java.util.Objects;
import javax.annotation.processing.Processor;

public record PortSpec(String name,
                       Class<?> portInterface,
                       String adapterPackagePattern,
                       Class<?> pluginClass,
                       Class<? extends Annotation> adapterAnnotation,
                       Class<? extends Processor> annotationProcessor,
                       Class<? extends Enum<?>> registryPathEnum) {

  public PortSpec { // compact canonical constructor
    requireNonBlank(name, "name");
    Objects.requireNonNull(portInterface, "portInterface");
    requireNonBlank(adapterPackagePattern, "adapterPackagePattern");
    Objects.requireNonNull(pluginClass, "pluginClass");
    Objects.requireNonNull(adapterAnnotation, "adapterAnnotation");
    Objects.requireNonNull(annotationProcessor, "processorClass");
    Objects.requireNonNull(registryPathEnum, "registryEnum");
  }

  private static void requireNonBlank(String s, String label) {
    if (s == null || s.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
  }

}
