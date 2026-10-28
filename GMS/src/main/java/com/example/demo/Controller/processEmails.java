package com.example.demo.Controller;

import com.example.demo.Service.EmailProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class processEmails {

    @Autowired
    private EmailProcessingService emailProcessingService;

    @Value("${gms.scheduler-secret:}")
    private String schedulerSecret;

    @PostMapping(value = "/process-emails", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> processEmails(@RequestHeader(value = "X-Scheduler-Secret", required = false) String secret) {
        if (!StringUtils.hasText(schedulerSecret) || !StringUtils.hasText(secret) || !MessageDigest.isEqual(schedulerSecret.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        emailProcessingService.processAllUsers();
        return ResponseEntity.ok("All users' digests processed");
    }
}
