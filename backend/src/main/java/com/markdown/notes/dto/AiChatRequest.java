package com.markdown.notes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI Chat Request DTO
 */
@Data
public class AiChatRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String context;
    
    private String conversationId;
    
    private String type = "chat"; // chat, enhance, summarize, translate
}
