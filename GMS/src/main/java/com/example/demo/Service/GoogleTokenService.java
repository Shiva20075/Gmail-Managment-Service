package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class GoogleTokenService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    public String getAccessToken(String refreshToken) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");
        Map<String, Object> response = restClient.post().uri("https://oauth2.googleapis.com/token").contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Google did not return an access token");
        }
        return (String) response.get("access_token");
    }
}
