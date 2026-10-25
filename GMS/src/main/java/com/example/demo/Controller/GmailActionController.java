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

    @PostMapping("/mark-read")
    public ResponseEntity<String> markAsRead(@RequestBody EmailActionRequest request, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.markAsRead(accessToken, request.getIds());
        return ResponseEntity.ok("Emails marked as read successfully.");

    }

    @PostMapping("/mark-important")
    public ResponseEntity<String> markAsImportant(@RequestBody EmailActionRequest request, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.markAsImportant(accessToken, request.getIds());
        return ResponseEntity.ok("Emails marked as important successfully.");

    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteAll(@RequestBody EmailActionRequest request, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.deleteAll(accessToken, request.getIds());
        return ResponseEntity.ok("Emails moved to Trash successfully.");

    }


    @GetMapping("/mark-read")
    public ResponseEntity<String> markAsReadLink(@RequestParam List<String> ids, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.markAsRead(accessToken, ids);
        return ResponseEntity.ok("Emails marked as read successfully.");

    }

    @GetMapping("/mark-important")
    public ResponseEntity<String> markAsImportantLink(@RequestParam List<String> ids, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.markAsImportant(accessToken, ids);
        return ResponseEntity.ok("Emails marked as important successfully.");

    }

    @GetMapping("/delete")
    public ResponseEntity<String> deleteAllLink(@RequestParam List<String> ids, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        gmailActionService.deleteAll(accessToken, ids);
        return ResponseEntity.ok("Emails moved to Trash successfully.");

    }
}