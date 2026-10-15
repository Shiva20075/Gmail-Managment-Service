
package com.example.demo.Controller;

import com.example.demo.Service.DailyDigestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class DailyDigestController {

    @Autowired
    private DailyDigestService dailyDigestService;

    @PostMapping("/api/digest/test")
    public Map<String, String> testDigest() {
        dailyDigestService.processAllUsers();
        return Map.of("message", "User processing completed. Check application logs.");
    }
}
