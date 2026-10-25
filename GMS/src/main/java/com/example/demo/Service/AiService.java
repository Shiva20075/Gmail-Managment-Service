
package com.example.demo.Service;

import com.example.demo.Model.AiResponse;
import com.example.demo.Model.Gmail;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final String MODEL = "claude-sonnet-4-6";
    private static final Set<String> VALID_CATEGORIES = Set.of("IMPORTANT", "NORMAL", "PROMOTIONAL_SPAM");
    private final AnthropicClient client;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final String userPrompt;

    public AiService(
            ObjectMapper objectMapper,
            @Value("${claude.api-key}") String apiKey,
            @Value("${claude.system-prompt}") String systemPrompt,
            @Value("${claude.user-prompt}") String userPrompt) {

        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public List<AiResponse> analyzeEmails(String account, List<Gmail> emails) {

        try {

            String prompt = userPrompt.formatted(account, objectMapper.writeValueAsString(emails));

            Message message = client.messages().create(MessageCreateParams.builder()
                            .model(MODEL)
                            .maxTokens(16000L)
                            .system(systemPrompt)
                            .addUserMessage(prompt)
                            .build()
            );

            if (message.stopReason().filter(StopReason.MAX_TOKENS::equals).isPresent()) {
                throw new IllegalStateException("Claude response was cut off by max_tokens");
            }

            String responseText = message.content().stream().flatMap(block -> block.text().stream()).map(TextBlock::text).collect(Collectors.joining());
            List<AiResponse> responses = objectMapper.readValue(cleanJsonResponse(responseText), new TypeReference<List<AiResponse>>() {});
            validateResponses(account, emails, responses);
            return responses;

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze Gmail messages: " + e.getMessage(), e);
        }
    }

    private String cleanJsonResponse(String response) {

        String result = response.trim();

        if (result.startsWith("```json")) {
            result = result.substring(7).trim();
        } else if (result.startsWith("```")) {
            result = result.substring(3).trim();
        }

        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3).trim();
        }

        return result;
    }

    private void validateResponses(String account, List<Gmail> emails, List<AiResponse> responses) {

        if (responses == null) {
            throw new IllegalStateException("Claude returned a null response");
        }

        Set<String> returnedCategories = new HashSet<>();
        Map<String, Integer> emailCounts = new HashMap<>();
        Map<String, String> expectedEmails = new HashMap<>();


        for (Gmail email : emails) {

            if (email.getId() == null || email.getSender() == null) {
                throw new IllegalStateException("Input email has a missing ID or sender");
            }

            if (expectedEmails.put(email.getId(), email.getSender()) != null) {
                throw new IllegalStateException("Duplicate input email ID: " + email.getId());
            }
        }

        for (AiResponse response : responses) {

            if (response == null || response.getCategory() == null || !VALID_CATEGORIES.contains(response.getCategory())) {
                throw new IllegalStateException("Claude returned an invalid category");
            }

            if (!returnedCategories.add(response.getCategory())) {
                throw new IllegalStateException("Claude returned a duplicate category: " + response.getCategory());
            }

            String[] ids = response.getIds() == null ? new String[0] : response.getIds();
            String[] senders = response.getSenders() == null ? new String[0] : response.getSenders();

            if (ids.length != senders.length) {
                throw new IllegalStateException("IDs and senders must have equal lengths");
            }

            for (int i = 0; i < ids.length; i++) {

                String id = ids[i];
                String sender = senders[i];

                if (id == null || sender == null || !expectedEmails.containsKey(id)) {
                    throw new IllegalStateException("Claude returned an unknown email ID " + "or missing sender");
                }

                if (!expectedEmails.get(id).equals(sender)) {
                    throw new IllegalStateException("Sender does not match original email ID: " + id);
                }
                emailCounts.merge(id, 1, Integer::sum);
            }

            response.setAccount(account);
            response.setIds(ids);
            response.setSenders(senders);

            if (response.getSummary() == null) {
                response.setSummary("");
            }
        }

        for (String id : expectedEmails.keySet()) {

            if (emailCounts.getOrDefault(id, 0) != 1) {
                throw new IllegalStateException("Each email must appear exactly once: " + id);
            }
        }

        if (emailCounts.size() != expectedEmails.size()) {
            throw new IllegalStateException("Claude omitted emails or returned unknown IDs");
        }
    }
}
