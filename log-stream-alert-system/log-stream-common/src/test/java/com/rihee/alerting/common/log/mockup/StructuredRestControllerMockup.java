package com.rihee.alerting.common.log.mockup;

import com.rihee.alerting.common.log.annotation.StructuredRestController;
import com.rihee.alerting.common.log.enums.StructuredLogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;

@StructuredRestController
public class StructuredRestControllerMockup {

    private static final Logger logger = LoggerFactory.getLogger(StructuredRestControllerMockup.class);

    @GetMapping("/mockup")
    public String mockResponse() {
        MDC.put(StructuredLogProperties.LOG_TYPE.getName(), "biz");
        logger.info("mockup service called");
        return "mock response";
    }
}
