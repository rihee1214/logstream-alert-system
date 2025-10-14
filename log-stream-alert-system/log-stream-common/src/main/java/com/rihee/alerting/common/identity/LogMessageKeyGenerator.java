package com.rihee.alerting.common.identity;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import com.fasterxml.uuid.UUIDGenerator;
import com.rihee.alerting.common.util.StringUtils;
import java.util.UUID;
import java.util.regex.Pattern;

public final class LogMessageKeyGenerator {

  /**
   * 로그 키 스키마 버전.
   */
  //  private static final String LOG_VERSION = "v1";

  private static final NoArgGenerator V7_GENERATOR = Generators.timeBasedEpochGenerator();

  private LogMessageKeyGenerator() {
  }

  public static String generate() {
    return V7_GENERATOR.generate().toString();
  }

}
