package com.greenfox.backend.modules.ai.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChatMemory implements ChatMemory {
    private final Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

    @Override
    public void add(String conversationId, Message message) {
        conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> history = conversationHistory.getOrDefault(conversationId, new ArrayList<>());
        if (lastN <= 0 || history.size() <= lastN) {
            return new ArrayList<>(history);
        }
        return new ArrayList<>(history.subList(history.size() - lastN, history.size()));
    }

    @Override
    public void clear(String conversationId) {
        conversationHistory.remove(conversationId);
    }
}
