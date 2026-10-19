package com.example.demo.Controller;

import com.example.demo.Model.GmsUser;
import com.example.demo.Repo.userInterface;
import com.example.demo.Service.GmailService;
import com.example.demo.Service.GoogleTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GmailTestController {

    @Autowired
    private userInterface userRepository;

    @Autowired
    private GoogleTokenService googleTokenService;

    @Autowired
    private GmailService gmailService;

    @GetMapping("/api/gmail/test")
    public Map<String, Object> testGmail() {

        List<GmsUser> users = userRepository.findAll();
        Map<String, Object> results = new LinkedHashMap<>();

        for (GmsUser user : users) {

            String email = user.getEmail();
            String refreshToken = user.getEncryptedRefreshToken();

            if (refreshToken == null || refreshToken.isBlank()) {
                results.put(email, "Skipped: no refresh token");
                continue;
            }

            try {
                String accessToken = googleTokenService.getAccessToken(refreshToken);
                Map<String, Map<String, Object>> userEmails = gmailService.fetchRecentEmails(accessToken);
                results.put(email, userEmails);

            } catch (Exception e) {
                System.err.println("Failed to fetch emails for user ID " + user.getId() + ": " + e.getMessage());
                results.put(email, "Failed to fetch emails");
            }
        }

        return results;
    }
}