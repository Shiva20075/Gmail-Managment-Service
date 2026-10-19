package com.example.demo.Service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GmailService {

    private final RestClient restClient = RestClient.create();

    public Map<String, Map<String, Object>> fetchRecentEmails(
            String accessToken) {

        Map<String, Map<String, Object>> emails = new LinkedHashMap<>();
        String nextPageToken = null;

        do {
            final String currentPageToken = nextPageToken;

            Map<?, ?> response = restClient.get().uri(uriBuilder -> {var builder = uriBuilder
                                .scheme("https")
                                .host("gmail.googleapis.com")
                                .path("/gmail/v1/users/me/messages")
                                .queryParam("q", "newer_than:1d")
                                .queryParam("maxResults", 500);

                        if (currentPageToken != null) {
                            builder.queryParam("pageToken", currentPageToken);
                        }

                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                break;
            }

            Object messagesObject = response.get("messages");

            if (messagesObject instanceof List<?> messages) {

                for (Object item : messages) {

                    if (!(item instanceof Map<?, ?> message)) {
                        continue;
                    }

                    Object idObject = message.get("id");

                    if (!(idObject instanceof String messageId)) {
                        continue;
                    }

                    try {
                        Map<String, Object> details = fetchEmailDetails(accessToken, messageId);

                        if (details != null) {
                            emails.put(messageId, details);
                        }

                    } catch (Exception e) {
                        System.err.println("Failed to fetch message " + messageId + ": " + e.getMessage());
                    }
                }
            }

            Object tokenObject = response.get("nextPageToken");
            nextPageToken = tokenObject == null ? null : tokenObject.toString();

        } while (nextPageToken != null);

        System.out.println("Total emails fetched: " + emails.size());
        return emails;
    }

    private Map<String, Object> fetchEmailDetails(String accessToken, String messageId) {

        Map<?, ?> detail = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("gmail.googleapis.com")
                        .path("/gmail/v1/users/me/messages/{id}")
                        .queryParam("format", "full")
                        .build(messageId))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        if (detail == null) {
            return null;
        }

        String from = "";
        Object payloadObject = detail.get("payload");

        if (payloadObject instanceof Map<?, ?> payload) {

            Object headersObject = payload.get("headers");

            if (headersObject instanceof List<?> headers) {

                for (Object item : headers) {

                    if (!(item instanceof Map<?, ?> header)) {
                        continue;
                    }

                    String name = String.valueOf(header.get("name"));
                    String value = String.valueOf(header.get("value"));

                    if (name.equalsIgnoreCase("From")) {
                        from = value;
                        break;
                    }
                }
            }
        }

        String body = extractBody(payloadObject);
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("headers", List.of(Map.of("name", "From", "value", from)));
        email.put("message", body);
        return email;
    }

    private String extractBody(Object payloadObject) {

        if (!(payloadObject instanceof Map<?, ?> payload)) {
            return "";
        }

        String mimeType = String.valueOf(payload.get("mimeType"));
        Object bodyObject = payload.get("body");

        if (bodyObject instanceof Map<?, ?> body) {

            Object dataObject = body.get("data");

            if (dataObject instanceof String data && !data.isBlank()) {

                try {
                    byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
                    String content = new String(decodedBytes, StandardCharsets.UTF_8);

                    if ("text/plain".equalsIgnoreCase(mimeType)) {
                        return content;
                    }

                    if ("text/html".equalsIgnoreCase(mimeType)) {
                        return htmlToText(content);
                    }

                } catch (IllegalArgumentException e) {
                    System.err.println(
                            "Failed to decode email body: "
                                    + e.getMessage());
                }
            }
        }

        Object partsObject = payload.get("parts");

        if (partsObject instanceof List<?> parts) {

            String htmlContent = "";

            for (Object partObject : parts) {

                if (!(partObject instanceof Map<?, ?> part)) {
                    continue;
                }

                String partMimeType = String.valueOf(part.get("mimeType"));
                Object filenameObject = part.get("filename");
                String filename = filenameObject == null ? "" : filenameObject.toString();

                if (!filename.isBlank()) {
                    continue;
                }

                String content = extractBody(part);

                if (content.isBlank()) {
                    continue;
                }

                if ("text/plain".equalsIgnoreCase(partMimeType)) {
                    return content;
                }

                if ("text/html".equalsIgnoreCase(partMimeType)) {
                    htmlContent = content;
                }
            }

            if (!htmlContent.isBlank()) {
                return htmlContent;
            }
        }

        return "";
    }

    private String htmlToText(String html) {

        if (html == null || html.isBlank()) {
            return "";
        }

        return html
                .replaceAll(
                        "(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p\\s*>", "\n")
                .replaceAll("(?i)</div\\s*>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n\\s*\\n+", "\n")
                .trim();
    }
}
