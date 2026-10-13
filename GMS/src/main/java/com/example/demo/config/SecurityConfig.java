package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationRequestResolver authorizationRequestResolver)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth.requestMatchers("/api/health").permitAll().anyRequest().authenticated()).oauth2Login(oauth -> oauth.authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(authorizationRequestResolver)).defaultSuccessUrl("/api/me", true));
        return http.build();
    }
}
