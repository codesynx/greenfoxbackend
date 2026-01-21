package com.greenfox.backend.modules.ai.memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssistantMessage implements Message {
    private String content;

    @Override
    public String getRole() {
        return "model";
    }
}
