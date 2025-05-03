package com.rihee.alerting.common.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class MockHttpServletRequestConfig {

    @Bean
    @Primary
    public HttpServletRequest httpServletRequest() {
        return mock(HttpServletRequest.class);
    }
}
