package com.rihee.alerting.loggingService.core;

import com.rihee.alerting.loggingService.collectors.LogCollectorSpec;
import com.rihee.alerting.loggingService.persistence.LogPersistenceSpec;
import com.rihee.alerting.loggingService.validators.LogValidatorSpec;
import java.util.Arrays;
import java.util.Map;

public enum LogProcessorSpecType {

  COLLECT("collect") {
    @Override
    public LogProcessorSpec createSpecInstance(Map<String, String> setting) {
      return new LogCollectorSpec(setting);
    }
  },
  VALIDATE("validate") {
    @Override
    public LogProcessorSpec createSpecInstance(Map<String, String> setting) {
      return new LogValidatorSpec(setting);
    }
  },
  PERSIST("persist") {
    @Override
    public LogProcessorSpec createSpecInstance(Map<String, String> setting) {
      return new LogPersistenceSpec(setting);
    }
  };

  private final String key;

  LogProcessorSpecType(String key) {
    this.key = key;
  }

  public static LogProcessorSpecType fromKey(String key) {
    return Arrays.stream(values())
        .filter(e -> e.key.equalsIgnoreCase(key))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown processor type key: " + key));
  }

  public abstract LogProcessorSpec createSpecInstance(Map<String, String> setting);
}
