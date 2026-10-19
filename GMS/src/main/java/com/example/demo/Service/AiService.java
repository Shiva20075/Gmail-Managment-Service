
package com.example.demo.Service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final AnthropicClient client =
            AnthropicOkHttpClient.fromEnv();

    @Value("${claude.system-prompt}")
    private String systemPrompt;

    public String analyzeEmail(
            String messageId,
            String from,
            String subject,
            String messageBody) {

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5)
                .maxTokens(8192L)
                .system(systemPrompt)
                .addUserMessage("""
                        Analyze the following email according to your
                        system instructions.

                        Return only the required JSON object.

                        Gmail message ID: %s
                        Sender: %s
                        Subject: %s

                        Email body:
                        %s
                        """.formatted(
                        messageId,
                        from,
                        subject,
                        messageBody))
                .build();

        Message response = client.messages().create(params);

        StringBuilder result = new StringBuilder();

        for (ContentBlock block : response.content()) {
            block.text().ifPresent(
                    textBlock -> result.append(textBlock.text()));
        }

        String jsonResponse = result.toString().trim();

        if (jsonResponse.isEmpty()) {
            throw new IllegalStateException(
                    "Claude returned an empty response");
        }

        return jsonResponse;
    }
}
