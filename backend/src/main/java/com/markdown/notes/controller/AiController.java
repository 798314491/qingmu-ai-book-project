package com.markdown.notes.controller;

import com.markdown.notes.common.Result;
import com.markdown.notes.dto.AiChatRequest;
import com.markdown.notes.dto.AiChatResponse;
import com.markdown.notes.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import com.markdown.notes.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * AI Chat Controller
 */
@Slf4j
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
    
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream chat with AI assistant")
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request, 
                                Authentication authentication) {
        // 在创建SSE连接之前验证权限
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to stream chat API");
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"请先登录\"}"));
                errorEmitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send unauthorized error message", ioException);
                errorEmitter.completeWithError(ioException);
            }
            return errorEmitter;
        }
        
        try {
            SseEmitter emitter = new SseEmitter(30000L); // 30秒超时
            Long userId = getUserId(authentication);
            
            // 异步处理流式响应
            aiService.streamChat(request, userId, emitter);
            
            return emitter;
        } catch (Exception e) {
            log.error("Failed to create stream chat emitter", e);
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"服务器内部错误\"}"));
                errorEmitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send error message", ioException);
                errorEmitter.completeWithError(ioException);
            }
            return errorEmitter;
        }
    }
    
    @PostMapping("/enhance")
    @Operation(summary = "Enhance text with AI")
    public Result<String> enhanceText(@RequestBody String text, Authentication authentication) {
        String enhancedText = aiService.enhanceText(text, getUserId(authentication));
        return Result.success(enhancedText);
    }
    
    @PostMapping(value = "/enhance/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream enhance text with AI")
    public SseEmitter streamEnhanceText(@RequestBody String text, Authentication authentication) {
        // 在创建SSE连接之前验证权限
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to stream enhance API");
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"请先登录\"}"));
                errorEmitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send unauthorized error message", ioException);
                errorEmitter.completeWithError(ioException);
            }
            return errorEmitter;
        }
        
        try {
            SseEmitter emitter = new SseEmitter(30000L);
            Long userId = getUserId(authentication);
            
            aiService.streamEnhanceText(text, userId, emitter);
            
            return emitter;
        } catch (Exception e) {
            log.error("Failed to create stream enhance emitter", e);
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"服务器内部错误\"}"));
                errorEmitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send error message", ioException);
                errorEmitter.completeWithError(ioException);
            }
            return errorEmitter;
        }
    }
    
    @PostMapping("/summarize")
    @Operation(summary = "Summarize content with AI")
    public Result<String> summarizeContent(@RequestBody String content, Authentication authentication) {
        String summary = aiService.summarizeContent(content, getUserId(authentication));
        return Result.success(summary);
    }
    
    @PostMapping(value = "/summarize/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream summarize content with AI")
    public SseEmitter streamSummarizeContent(@RequestBody String content, Authentication authentication) {
        // 在创建SSE连接之前验证权限
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to stream summarize API");
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"请先登录\"}"));
                errorEmitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send unauthorized error message", ioException);
                errorEmitter.completeWithError(ioException);
            }
            return errorEmitter;
        }
        
        try {
            SseEmitter emitter = new SseEmitter(30000L);
            Long userId = getUserId(authentication);
            
            aiService.streamSummarizeContent(content, userId, emitter);
            
            return emitter;
        } catch (Exception e) {
            log.error("Failed to create stream summarize emitter", e);
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"服务器内部错误\"}"));
                errorEmitter.complete();
            } catch (IOException ioException) {
                log.error("Failed to send error message", ioException);
                errorEmitter.completeWithError(ioException);
            }
            return errorEmitter;
        }
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
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
