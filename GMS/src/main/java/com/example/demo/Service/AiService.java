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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final String MODEL = "claude-sonnet-4-6";
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
            Message message = client.messages().create(MessageCreateParams.builder().model(MODEL).maxTokens(16000L).system(systemPrompt).addUserMessage(prompt).build());

            if (message.stopReason().filter(StopReason.MAX_TOKENS::equals).isPresent()) {
                throw new IllegalStateException("Claude response was cut off by max_tokens");
            }

            String responseText = message.content().stream().flatMap(block -> block.text().stream()).map(TextBlock::text).collect(Collectors.joining());

            System.out.println("Claude stop reason: " + message.stopReason());
            System.out.println("Claude raw response: " + responseText);

            List<AiResponse> responses = objectMapper.readValue(cleanJsonResponse(responseText), new TypeReference<List<AiResponse>>() {});

            for (AiResponse response : responses) {
                response.setAccount(account);
            }

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
}
