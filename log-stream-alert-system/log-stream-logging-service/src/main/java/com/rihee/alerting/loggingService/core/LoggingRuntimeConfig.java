package com.rihee.alerting.loggingService.core;

import com.rihee.alerting.loggingService.collectors.LogCollector;
import com.rihee.alerting.loggingService.collectors.LogCollectorSpec;
import com.rihee.alerting.loggingService.persistence.LogPersistence;
import com.rihee.alerting.loggingService.persistence.LogPersistenceSpec;
import com.rihee.alerting.loggingService.validators.LogValidator;
import com.rihee.alerting.loggingService.validators.LogValidatorSpec;
import java.util.Properties;

public class LoggingRuntimeConfig {

  private LogCollectorSpec collectorSpec;
  private LogValidatorSpec validatorSpec;
  private LogPersistenceSpec persistenceSpec;

  private LoggingRuntimeConfig(Properties setting) {
    this.collectorSpec = LogCollectorSpec.from(setting);
    this.validatorSpec = LogValidatorSpec.from(setting);
    this.persistenceSpec = LogPersistenceSpec.from(setting);
  }

  public static LoggingRuntimeConfig from(Properties setting) {
    return new LoggingRuntimeConfig(setting);
  }

  public LogCollector getCollectorInstance() {
    // TODO LogCollectorSpec 객체로부터 setting에 걸맞는 구현체 클래스의 인스턴스 생성 그 후 worker 팩터리 메서드에 전달
    return null;
  }

  public LogValidator getValidatorInstance() {
    // TODO LogValidatorSpec 객체로부터 setting에 걸맞는 구현체 클래스의 인스턴스 생성 그 후 worker 팩터리 메서드에 전달
    return null;
  }

  public LogPersistence getPersistenceInstance() {
    // TODO LogPersistenceSpec 객체로부터 setting에 걸맞는 구현체 클래스의 인스턴스 생성 그 후 worker 팩터리 메서드에 전달
    return null;
  }
}
