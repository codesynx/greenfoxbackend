package com.greenfox.backend.modules.ai.memory;

import java.util.List;

public interface ChatMemory {
    void add(String conversationId, Message message);
    List<Message> get(String conversationId, int lastN);
    void clear(String conversationId);
}
