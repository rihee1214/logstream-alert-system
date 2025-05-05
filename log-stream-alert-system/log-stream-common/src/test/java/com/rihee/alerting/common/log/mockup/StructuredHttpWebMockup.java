package com.rihee.alerting.common.log.mockup;

import com.rihee.alerting.common.annotation.StructuredGetMapping;
import com.rihee.alerting.common.log.StructuredLogger;
import com.rihee.alerting.common.log.StructuredLoggerFactory;
import com.rihee.alerting.common.log.enums.LogType;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StructuredHttpWebMockup {

    private static final StructuredLogger logger = StructuredLoggerFactory.getLogger(StructuredHttpWebMockup.class);

    @StructuredGetMapping(spanLabel = "getMappingTest", value = "getMappingTestMockup")
    public String mockResponse() {
        logger.info(LogType.BIZ, "mockup service called");
        return "mock response";
    }
}
