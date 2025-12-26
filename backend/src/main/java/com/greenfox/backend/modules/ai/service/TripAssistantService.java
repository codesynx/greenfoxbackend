package com.greenfox.backend.modules.ai.service;

import com.greenfox.backend.modules.ai.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripAssistantService {

    private final ChatClient.Builder chatClientBuilder;

    public AiChatResponse chat(String message) {
        // Clear previous context
        ResortSearchContext.clear();
        
        ChatClient chatClient = chatClientBuilder
                .defaultSystem("You are a premium travel concierge for the GreenFox booking app. " +
                        "You help users find the best resorts in Kazakhstan. " +
                        "You MUST use the 'searchResorts' tool when users ask for resort suggestions or information. " +
                        "Do not make up resort information. Always use the tool to find real data. " +
                        "If you find resorts, summarize them in a friendly way, highlighting their amenities and price.")
                .defaultFunctions("searchResorts")
                .build();

        String reply = chatClient.prompt()
                .user(message)
                .call()
                .content();

        return new AiChatResponse(reply, ResortSearchContext.getResorts());
    }
}
