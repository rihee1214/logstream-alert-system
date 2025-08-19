package com.rihee.alerting.loggingService.core.plan;

import com.rihee.alerting.loggingService.core.plugin.LogProcessorPlugin;
import com.rihee.alerting.loggingService.core.plugin.LogCollectorPlugin;
import com.rihee.alerting.loggingService.core.plugin.LogPersistencePlugin;
import com.rihee.alerting.loggingService.core.plugin.LogValidatorPlugin;
import java.util.Arrays;
import java.util.Map;

public enum LogProcessorPluginPlanner {

  COLLECT("collect") {
    @Override
    public LogProcessorPlugin createSpecInstance(Map<String, String> setting) {
      return new LogCollectorPlugin(setting);
    }
  },
  VALIDATE("validate") {
    @Override
    public LogProcessorPlugin createSpecInstance(Map<String, String> setting) {
      return new LogValidatorPlugin(setting);
    }
  },
  PERSIST("persist") {
    @Override
    public LogProcessorPlugin createSpecInstance(Map<String, String> setting) {
      return new LogPersistencePlugin(setting);
    }
  };

  private final String key;

  LogProcessorPluginPlanner(String key) {
    this.key = key;
  }

  public static LogProcessorPluginPlanner fromKey(String key) {
    return Arrays.stream(values())
        .filter(e -> e.key.equalsIgnoreCase(key))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown processor type key: " + key));
  }

  public abstract LogProcessorPlugin createSpecInstance(Map<String, String> setting);
}
