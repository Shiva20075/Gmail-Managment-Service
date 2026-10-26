
package com.example.demo.Service;

import com.example.demo.Model.AiResponse;
import com.example.demo.Model.Gmail;
import com.example.demo.Model.GmsUser;
import com.example.demo.Repo.userInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EmailProcessingService {

    @Autowired
    private GmailService gmailService;

    @Autowired
    private AiService aiService;

    @Autowired
    private DigestTemplateService digestTemplateService;

    @Autowired
    private GmailSendService gmailSendService;

    @Autowired
    private userInterface userRepository;

    @Autowired
    private GoogleTokenService googleTokenService;

    public String processEmails(String account, String accessToken) {

        List<Gmail> emails = gmailService.fetchRecentEmails(accessToken);
        List<AiResponse> aiResponses = aiService.analyzeEmails(account, emails);
        String html = digestTemplateService.generateDigest(account, aiResponses);
        gmailSendService.sendDigest(account, accessToken, html);

        return html;
    }

    public void processAllUsers() {

        List<GmsUser> users = userRepository.findAll();

        for (GmsUser user : users) {
            try {
                String email = user.getEmail();
                String refreshToken = user.getEncryptedRefreshToken();

                if (!StringUtils.hasText(refreshToken)) {
                    System.err.println("Skipping user because refresh token is missing: " + email);
                    continue;
                }

                String accessToken = googleTokenService.getAccessToken(refreshToken);
                processEmails(email, accessToken);
                System.out.println("Digest processed successfully for: " + email);

            } catch (Exception e) {
                System.err.println("Failed to process user: " + user.getEmail());e.printStackTrace();
            }
        }
    }
}
