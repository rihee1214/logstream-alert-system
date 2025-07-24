package com.rihee.alerting.loggingService.collectors;

import java.util.List;
import java.util.Properties;

public abstract class LogCollector {

  public abstract List<String> getLogDatas();

  public interface Builder<T extends LogCollector> {
    Builder<T> withProperties(Properties setting);
    T build();
  }
}
