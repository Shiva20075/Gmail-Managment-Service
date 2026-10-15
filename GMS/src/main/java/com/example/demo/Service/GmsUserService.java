
package com.example.demo.Service;

import com.example.demo.Model.GmsUser;
import com.example.demo.Repo.userInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GmsUserService {

    @Autowired
    private userInterface userRepository;

    public List<GmsUser> getAllUsers() {
        return userRepository.findAll();
    }

    public GmsUser saveUser(GmsUser user) {
        return userRepository.save(user);
    }

    public GmsUser saveGoogleUser(String email, String refreshToken) {

        GmsUser user = userRepository.findByEmail(email).orElseGet(() -> {
                    GmsUser newUser = new GmsUser();
                    newUser.setEmail(email);
                    return newUser;
                });

        if (refreshToken != null && !refreshToken.isBlank()) {
            user.setEncryptedRefreshToken(refreshToken);
        }

        return userRepository.save(user);
    }
}
