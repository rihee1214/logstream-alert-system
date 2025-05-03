package com.rihee.alerting.common.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.rihee.alerting.common.config.WebConfig;
import com.rihee.alerting.common.configuration.MockHttpServletRequestConfig;
import com.rihee.alerting.common.log.appender.MemoryAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=dev")
@AutoConfigureMockMvc
@Import({MockHttpServletRequestConfig.class, WebConfig.class})
public class StructuredHttpServerTests {

    @Autowired
    private MockMvc mockMvc;

    private MemoryAppender memoryAppender;
    private Logger logger;

    // append가 불가능. 결국 안에서 로그를 찍어야 한다는건데...
    @BeforeEach
    void setUp() {
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        memoryAppender.start();

        logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.addAppender(memoryAppender);
    }

    @AfterEach
    void tearDown() {
        memoryAppender.stop();
        logger.detachAppender(memoryAppender);
    }

    @Test
    void mockupService_shouldSetMdcFields() throws Exception {

        mockMvc.perform(get("/getMappingTestMockup")).andExpect(status().isOk());

        boolean foundTraceId = memoryAppender.getLoggedEvents().stream()
                .anyMatch(event -> event.getMDCPropertyMap().containsKey("traceId"));

        boolean foundSpanId = memoryAppender.getLoggedEvents().stream()
                .anyMatch(event -> event.getMDCPropertyMap().containsKey("spanId"));

        boolean foundParentSpanId = memoryAppender.getLoggedEvents().stream()
                .anyMatch(event -> event.getMDCPropertyMap().containsKey("parentSpanId"));

        assertThat(foundTraceId).isTrue();
        assertThat(foundSpanId).isTrue();
        assertThat(foundParentSpanId).isFalse();
    }
}
