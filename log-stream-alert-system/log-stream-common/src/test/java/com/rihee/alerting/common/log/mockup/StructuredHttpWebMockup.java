package com.rihee.alerting.common.log.mockup;

import com.rihee.alerting.common.annotation.StructuredGetMapping;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.enums.LogType;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StructuredHttpWebMockup {

    private static final StructuredLogger logger = StructuredLoggerFactory.getLogger(StructuredHttpWebMockup.class);

    @StructuredGetMapping(spanLabel = "getMappingTest", value = "getMappingTestMockup")
    public String mockResponse() {
        logger.info(LogType.BIZ, "mockup service called");
        System.out.println("Logger class = " + logger.getClass());
        System.out.println("Logger class = " + LoggerFactory.getLogger(this.getClass()).getClass());
        return "mock response";
    }
}
