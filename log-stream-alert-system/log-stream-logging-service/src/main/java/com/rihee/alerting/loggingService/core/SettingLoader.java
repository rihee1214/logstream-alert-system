package com.rihee.alerting.loggingService.core;

import java.util.Properties;

public class SettingLoader {

  public LoggingRuntimeConfig loadRuntimeSetting() {
    Properties setting = new Properties();
    // TODO properties 를 제대로 로딩하는 로직이 나와야 한다.
    return LoggingRuntimeConfig.from(setting);
  }

}
