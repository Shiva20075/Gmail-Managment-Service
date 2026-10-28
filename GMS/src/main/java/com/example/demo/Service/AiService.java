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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final String MODEL = "claude-sonnet-4-6";
    private static final int MAX_ATTEMPTS = 2;
    private static final Set<String> VALID_CATEGORIES = Set.of("IMPORTANT", "NORMAL", "PROMOTIONAL_SPAM");
    private final AnthropicClient client;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final String userPrompt;

    public AiService(ObjectMapper objectMapper, @Value("${claude.api-key}") String apiKey, @Value("${claude.system-prompt}") String systemPrompt, @Value("${claude.user-prompt}") String userPrompt) {
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public List<AiResponse> analyzeEmails(String account, List<Gmail> emails) {
        try {
            if (emails == null || emails.isEmpty()) {
                return new ArrayList<>();
            }
            String prompt = userPrompt.formatted(account, objectMapper.writeValueAsString(emails));
            String retryInstruction = "";
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                try {
                    Message message = client.messages().create(MessageCreateParams.builder().model(MODEL).maxTokens(16000L).system(systemPrompt).addUserMessage(prompt + retryInstruction).build());
                    if (message.stopReason().filter(StopReason.MAX_TOKENS::equals).isPresent()) {
                        throw new IllegalStateException("Claude response was cut off by max_tokens");
                    }
                    String responseText = message.content().stream().flatMap(block -> block.text().stream()).map(TextBlock::text).collect(Collectors.joining());
                    System.out.println("Claude stop reason: " + message.stopReason());
                    System.out.println("Claude raw response: " + responseText);
                    List<AiResponse> responses = objectMapper.readValue(cleanJsonResponse(responseText), new TypeReference<List<AiResponse>>() {});
                    validateResponses(account, emails, responses);
                    return responses;
                } catch (Exception e) {
                    System.err.println("Claude attempt " + attempt + " failed: " + e.getMessage());
                    if (attempt == MAX_ATTEMPTS) {
                        throw e;
                    }
                    retryInstruction = "\n\nYour previous response failed validation. Analyze the original email list again and return corrected JSON. Every original email ID must appear exactly once. Do not duplicate, omit, or invent IDs. Use only IMPORTANT, NORMAL, PROMOTIONAL_SPAM. The ids and senders arrays must have equal lengths, and each sender must correspond to its email ID. Return one combined summary per category. Return valid JSON only, without Markdown.";
                }
            }
            throw new IllegalStateException("Claude analysis failed after retries");
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
        Map<String, String> expectedEmails = new HashMap<>();
        for (Gmail email : emails) {
            if (email.getId() == null || email.getSender() == null) {
                throw new IllegalStateException("Input email has a missing ID or sender");
            }
            if (expectedEmails.put(email.getId(), email.getSender()) != null) {
                throw new IllegalStateException("Duplicate input email ID: " + email.getId());
            }
        }
        Set<String> returnedCategories = new HashSet<>();
        Set<String> processedIds = new HashSet<>();
        for (AiResponse response : responses) {
            if (response == null || response.getCategory() == null || !VALID_CATEGORIES.contains(response.getCategory())) {
                throw new IllegalStateException("Claude returned an invalid category");
            }
            if (!returnedCategories.add(response.getCategory())) {
                throw new IllegalStateException("Claude returned a duplicate category: " + response.getCategory());
            }
            String[] ids = response.getIds() == null ? new String[0] : response.getIds();
            List<String> correctSenders = new ArrayList<>();
            for (String id : ids) {
                if (id == null || !expectedEmails.containsKey(id)) {
                    throw new IllegalStateException("Claude returned an unknown email ID");
                }
                if (!processedIds.add(id)) {
                    throw new IllegalStateException("Each email must appear exactly once: " + id);
                }
                correctSenders.add(expectedEmails.get(id));
            }
            response.setAccount(account);
            response.setIds(ids);
            response.setSenders(correctSenders.toArray(new String[0]));
            if (response.getSummary() == null) {
                response.setSummary("");
            }
        }
        Set<String> missingIds = new HashSet<>(expectedEmails.keySet());
        missingIds.removeAll(processedIds);
        if (!missingIds.isEmpty()) {
            throw new IllegalStateException("Claude omitted emails. Missing IDs: " + missingIds);
        }
    }
}
