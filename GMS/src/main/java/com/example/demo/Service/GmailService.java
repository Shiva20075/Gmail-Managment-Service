
package com.example.demo.Service;

import com.example.demo.Model.Gmail;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GmailService {

    private static final String GMAIL_API = "https://gmail.googleapis.com/gmail/v1/users/me";
    private final RestClient restClient = RestClient.create(GMAIL_API);

    public List<Gmail> fetchRecentEmails(String accessToken) {

        List<Gmail> emails = new ArrayList<>();
        String pageToken = null;

        try {
            
            do {
                
                String token = pageToken;
                JsonNode page = restClient.get().uri(uri -> {uri.path("/messages").queryParam("q", "newer_than:1d in:anywhere").queryParam("maxResults", 100);
                            if (token != null) {
                                uri.queryParam("pageToken", token);
                            }
                            return uri.build();
                        })
                        .headers(h -> h.setBearerAuth(accessToken))
                        .retrieve()
                        .body(JsonNode.class);

                if (page == null) {
                    break;
                }

                for (JsonNode message : page.path("messages")) {
                    
                    String messageId = message.path("id").asString();

                    if (!messageId.isBlank()) {
                        emails.add(fetchSingleEmail(messageId, accessToken));
                    }
                }

                String next = page.path("nextPageToken").asString();
                pageToken = next.isBlank() ? null : next;

            } while (pageToken != null);

            System.out.println("Total emails fetched: " + emails.size());
            return emails;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch emails from Gmail: " + e.getMessage(),
                    e
            );
        }
    }

    private Gmail fetchSingleEmail(String messageId, String accessToken) {

        JsonNode message = restClient.get().uri("/messages/{id}?format=full", messageId).headers(h -> h.setBearerAuth(accessToken)).retrieve().body(JsonNode.class);
        JsonNode payload = message == null ? null : message.path("payload");

        String sender = "";
        String subject = "";
        String date = "";

        if (payload != null) {
            for (JsonNode header : payload.path("headers")) {
                String value = header.path("value").asString();

                switch (header.path("name").asString().toLowerCase()) {
                    case "from" -> sender = value;
                    case "subject" -> subject = value;
                    case "date" -> date = value;
                }
            }
        }

        return new Gmail(messageId, sender, subject, date, payload == null ? "" : extractBody(payload));
    }

    private String extractBody(JsonNode payload) {

        String plainText = extractMimePart(payload, "text/plain");

        if (!plainText.isBlank()) {
            return plainText;
        }

        return htmlToText(extractMimePart(payload, "text/html"));
    }

    private String extractMimePart(JsonNode part, String desiredMimeType) {

        if (desiredMimeType.equalsIgnoreCase(part.path("mimeType").asString())) {
            String data = part.path("body").path("data").asString();

            if (!data.isBlank()) {
                return decodeBase64Url(data);
            }
        }

        for (JsonNode child : part.path("parts")) {
            String result = extractMimePart(child, desiredMimeType);

            if (!result.isBlank()) {
                return result;
            }
        }

        return "";
    }

    private String decodeBase64Url(String data) {

        try {
            
            byte[] decoded = Base64.getUrlDecoder().decode(data);
            return new String(decoded, StandardCharsets.UTF_8);
            
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Could not decode Gmail message body", e);
        }
    }

    private String htmlToText(String html) {

        return html
                .replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|tr|h[1-6])\\s*>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n\\s*\\n+", "\n")
                .trim();
    }
}
