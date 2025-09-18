package com.markdown.notes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI Chat Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    
    private String id;
    
    private String conversationId;
    
    private String message;
    
    private String response;
    
    private String type;
    
    private LocalDateTime timestamp;
    
    private Long userId;
}
