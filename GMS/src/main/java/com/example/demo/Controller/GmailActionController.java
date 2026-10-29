package com.example.demo.Controller;

import com.example.demo.Model.EmailActionRequest;
import com.example.demo.Service.GmailActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class GmailActionController {

    @Autowired
    private GmailActionService gmailActionService;

    @GetMapping("/mark-read")
    public ResponseEntity<String> markAsRead( @RequestParam List<String> ids,@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.markAsRead(accessToken, ids);
        return ResponseEntity.ok("Emails marked as read successfully.");
        
    }

    @GetMapping("/mark-important")
    public ResponseEntity<String> markAsImportant( @RequestParam List<String> ids,@RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.markAsImportant(accessToken, ids);
        return ResponseEntity.ok("Emails marked as important successfully.");
    }

    @GetMapping("/delete")
    public ResponseEntity<String> deleteAll( @RequestParam List<String> ids,@RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.deleteAll(accessToken, ids);
        return ResponseEntity.ok("Emails moved to Trash successfully.");
    }
}
