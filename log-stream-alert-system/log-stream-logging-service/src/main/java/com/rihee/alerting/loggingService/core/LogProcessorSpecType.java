package com.rihee.alerting.loggingService.core;

import java.util.Arrays;
import java.util.Optional;

public enum LogProcessorSpecType {

  COLLECT("collect"),
  VALIDATE("validate"),
  PERSIST("persist");

  private final String key;

  LogProcessorSpecType(String key) {
    this.key = key;
  }

  public static Optional<LogProcessorSpecType> fromKey(String key) {
    return Arrays.stream(values())
        .filter(e -> e.key.equalsIgnoreCase(key))
        .findFirst();
  }
}
