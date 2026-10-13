package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

@Configuration
public class OAuth2AuthorizationConfig {

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository repository) {

        var resolver = new DefaultOAuth2AuthorizationRequestResolver(
                repository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(builder ->
                builder.additionalParameters(params -> {
                    params.put("access_type", "offline");
                    params.put("prompt", "consent");
                }));

        return resolver;
    }
}