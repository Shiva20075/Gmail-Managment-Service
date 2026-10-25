package com.example.demo.Controller;
import com.example.demo.Service.EmailProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class processEmails {

    @Autowired
    private EmailProcessingService emailProcessingService;

    @GetMapping(value = "/process-emails", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> processEmails(@AuthenticationPrincipal OidcUser user, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String account = user.getEmail();
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String html = emailProcessingService.processEmails(account, accessToken);

        return ResponseEntity.ok(html);
    }
}
