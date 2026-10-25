
package com.example.demo.Service;

import com.example.demo.Model.AiResponse;
import com.example.demo.Model.Gmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String processEmails(String account, String accessToken) {

        List<Gmail> emails = gmailService.fetchRecentEmails(accessToken);
        List<AiResponse> aiResponses = aiService.analyzeEmails(account, emails);
        String html = digestTemplateService.generateDigest(account, aiResponses);
        gmailSendService.sendDigest(account, accessToken, html);

        return html;
    }
}
