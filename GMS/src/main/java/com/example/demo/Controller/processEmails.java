package com.example.demo.Controller;

import com.example.demo.Service.EmailProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class processEmails {

    @Autowired
    private EmailProcessingService emailProcessingService;

    @GetMapping(value = "/process-emails", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> processEmails() {
        emailProcessingService.processAllUsers();
        return ResponseEntity.ok("All users' digests processed");
    }
}
