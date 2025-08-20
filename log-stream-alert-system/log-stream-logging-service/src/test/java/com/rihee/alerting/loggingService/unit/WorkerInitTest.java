package com.rihee.alerting.loggingService.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.rihee.alerting.loggingService.core.runtime.LoggingRuntimeConfig;
import com.rihee.alerting.loggingService.core.runtime.SettingLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerInitTest {

  private final Logger log = LoggerFactory.getLogger(WorkerInitTest.class);

  /**
   * 빌드, CI 테스트 등 단위 테스트에서는 PROGRAM_MODE를 dev 혹은 test로 둘것.
   */
  @Test
  @DisplayName("런타임 초기화 결과 로그 파싱 및 검증")
  void processorInstanceTest() {
    LoggingRuntimeConfig config = SettingLoader.loadRuntimeSettingFromClasspath();

    String initResult = config.toString();

    log.info("init result : {}", initResult);

    // then: threadCount 존재/숫자 확인 (옵션)
    assertThat(initResult).contains("threadCount=");

    // processors 키 존재/파이프라인 단서 확인 (옵션)
    assertThat(initResult)
        .contains("processors=")
        .contains("LogCollectorPlugin(")
        .contains("LogValidatorPlugin(")
        .contains("LogPersistencePlugin(");

    // initTime=NNN ms 파싱
    Pattern p = Pattern.compile("initTime\\s*=\\s*(\\d+)\\s*ms");
    Matcher m = p.matcher(initResult);
    assertThat(m.find())
        .as("toString() must contain 'initTime=<number> ms' but was: %s", initResult)
        .isTrue();
    long initMs = Long.parseLong(m.group(1));
    assertThat(initMs).as("init time should be < 5000 ms, actual=%d", initMs)
        .isLessThan(5_000);
  }
}
