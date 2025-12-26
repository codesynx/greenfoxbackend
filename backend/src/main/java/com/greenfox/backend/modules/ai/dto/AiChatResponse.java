package com.greenfox.backend.modules.ai.dto;

import com.greenfox.backend.modules.resort.entity.Resort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String replyText;
    private List<Resort> recommendations;
}
