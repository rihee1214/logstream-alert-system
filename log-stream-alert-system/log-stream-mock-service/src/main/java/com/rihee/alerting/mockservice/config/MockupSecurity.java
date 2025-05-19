package com.rihee.alerting.mockservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class MockupSecurity {

  private static final String MOCK_AUTH_TOKEN_HEADER = "X-Auth-Token";

  @Value("${mockup.token:__UNDEFINED__}")
  private String mockAuthToken;

  @Bean
  @Order(0)
  public SecurityFilterChain permitFilter(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth
        -> auth.anyRequest()
            .access((authSupplier, context) -> {
              String token = context.getRequest().getHeader(MOCK_AUTH_TOKEN_HEADER);
              return new AuthorizationDecision(mockAuthToken.equals(token));
            }))
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
