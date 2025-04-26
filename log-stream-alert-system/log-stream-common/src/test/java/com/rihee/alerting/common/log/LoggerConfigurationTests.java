package com.rihee.alerting.common.log;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoggerConfigurationTests {

    private static final Logger log = LoggerFactory.getLogger(LoggerConfigurationTests.class);

    @Test
    void printLogsAccordingToLogbackConfiguration() {
        MDC.put("logtype", "sys");
        MDC.put("service", "log-test");
        MDC.put("host", "tester");
        MDC.put("container", "tester");

        log.debug("test-message");

        MDC.remove("logtype");
        MDC.remove("service");
        MDC.remove("host");
        MDC.remove("container");
    }
}
