package com.markdown.notes.controller;

import com.markdown.notes.common.Result;
import com.markdown.notes.dto.AiChatRequest;
import com.markdown.notes.dto.AiChatResponse;
import com.markdown.notes.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI Chat Controller
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI assistant APIs")
public class AiController {
    
    private final AiService aiService;
    
    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request, 
                                       Authentication authentication) {
        AiChatResponse response = aiService.chat(request, getUserId(authentication));
        return Result.success(response);
    }
    
    @PostMapping("/enhance")
    @Operation(summary = "Enhance text with AI")
    public Result<String> enhanceText(@RequestBody String text, Authentication authentication) {
        String enhancedText = aiService.enhanceText(text, getUserId(authentication));
        return Result.success(enhancedText);
    }
    
    @PostMapping("/summarize")
    @Operation(summary = "Summarize content with AI")
    public Result<String> summarizeContent(@RequestBody String content, Authentication authentication) {
        String summary = aiService.summarizeContent(content, getUserId(authentication));
        return Result.success(summary);
    }
    
    @PostMapping("/translate")
    @Operation(summary = "Translate text with AI")
    public Result<String> translateText(@RequestParam String text, 
                                        @RequestParam String targetLanguage,
                                        Authentication authentication) {
        String translatedText = aiService.translateText(text, targetLanguage, getUserId(authentication));
        return Result.success(translatedText);
    }
    
    @GetMapping("/conversations")
    @Operation(summary = "Get conversation history")
    public Result<List<AiChatResponse>> getConversations(Authentication authentication) {
        List<AiChatResponse> conversations = aiService.getConversationHistory(getUserId(authentication));
        return Result.success(conversations);
    }
    
    @DeleteMapping("/conversations")
    @Operation(summary = "Clear conversation history")
    public Result<Void> clearConversations(Authentication authentication) {
        aiService.clearConversationHistory(getUserId(authentication));
        return Result.success();
    }
    
    private Long getUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
