
package com.example.demo.Service;

import com.example.demo.Model.GmsUser;
import com.example.demo.Repo.userInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyDigestService {

    @Autowired
    private userInterface userRepository;

    @Autowired
    private GoogleTokenService googleTokenService;

    public void processAllUsers() {

        List<GmsUser> users = userRepository.findAll();

        for (GmsUser user : users) {
            try {
                String email = user.getEmail();
                String refreshToken = user.getEncryptedRefreshToken();

                if (refreshToken == null || refreshToken.isBlank()) {
                    System.out.println("Skipping user without refresh token: " + email);
                    continue;
                }

                String accessToken = googleTokenService.getAccessToken(refreshToken);

                if (accessToken != null && !accessToken.isBlank()) {
                    System.out.println("Access token obtained for: " + email);
                }

            } catch (Exception e) {
                System.err.println("Failed to process user ID: " + user.getId() + ". Error: " + e.getMessage());
            }
        }
    }
}
