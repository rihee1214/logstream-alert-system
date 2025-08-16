package com.rihee.alerting.loggingService.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import org.junit.jupiter.api.Test;

public class WorkerInitTest {

  @Test
  void processorInstanceTest() {
    LoggingRuntimeConfig config = SettingLoader.loadRuntimeSettingFromClasspath();

    assertEquals(8, config.getWorkerThreadCount());
  }
}
