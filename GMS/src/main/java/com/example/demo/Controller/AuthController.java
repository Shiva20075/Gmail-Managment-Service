package com.example.demo.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/api/me")
    public Map<String, String> currentUser (@AuthenticationPrincipal OidcUser user) {

        return Map.of(
                "name", user.getFullName(),
                "email", user.getEmail(),
                "message", "Google login successful"
        );
    }
}