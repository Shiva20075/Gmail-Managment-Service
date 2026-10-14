
package com.example.demo.config;

import com.example.demo.Service.GmsUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private GmsUserService gmsUserService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OidcUser user = (OidcUser) authentication.getPrincipal();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        String refreshToken = null;

        if (client != null && client.getRefreshToken() != null) {
            refreshToken = client.getRefreshToken().getTokenValue();
        }

        gmsUserService.saveGoogleUser(user.getEmail(), refreshToken);
        response.sendRedirect("/api/me");
    }
}
