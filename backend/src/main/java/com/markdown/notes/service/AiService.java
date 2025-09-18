package com.markdown.notes.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.markdown.notes.dto.AiChatRequest;
import com.markdown.notes.dto.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Service for Dashscope integration
 */
@Slf4j
@Service
public class AiService {
    
    @Value("${alibaba.dashscope.api-key}")
    private String apiKey;
    
    @Value("${alibaba.dashscope.model}")
    private String model;
    
    // 简单的内存存储对话历史（生产环境应使用数据库）
    private final ConcurrentHashMap<Long, List<AiChatResponse>> conversationHistory = new ConcurrentHashMap<>();
    
    /**
     * Chat with AI
     */
    public AiChatResponse chat(AiChatRequest request, Long userId) {
        try {
            String response = callDashscopeApi(request.getMessage(), request.getContext());
            
            AiChatResponse chatResponse = AiChatResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .conversationId(request.getConversationId())
                    .message(request.getMessage())
                    .response(response)
                    .type(request.getType())
                    .timestamp(LocalDateTime.now())
                    .userId(userId)
                    .build();
            
            // 保存到历史记录
            conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(chatResponse);
            
            return chatResponse;
        } catch (Exception e) {
            log.error("AI chat failed", e);
            return AiChatResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .message(request.getMessage())
                    .response("抱歉，AI服务暂时不可用，请稍后重试。错误信息：" + e.getMessage())
                    .type(request.getType())
                    .timestamp(LocalDateTime.now())
                    .userId(userId)
                    .build();
        }
    }
    
    /**
     * Enhance text
     */
    public String enhanceText(String text, Long userId) {
        String prompt = "请帮我润色以下文字，使其更加优雅和专业：\n\n" + text;
        try {
            return callDashscopeApi(prompt, null);
        } catch (Exception e) {
            log.error("Text enhancement failed", e);
            return "文字润色功能暂时不可用：" + e.getMessage();
        }
    }
    
    /**
     * Summarize content
     */
    public String summarizeContent(String content, Long userId) {
        String prompt = "请帮我总结以下内容的要点：\n\n" + content;
        try {
            return callDashscopeApi(prompt, null);
        } catch (Exception e) {
            log.error("Content summarization failed", e);
            return "内容总结功能暂时不可用：" + e.getMessage();
        }
    }
    
    /**
     * Translate text
     */
    public String translateText(String text, String targetLanguage, Long userId) {
        String prompt = String.format("请将以下文字翻译成%s：\n\n%s", targetLanguage, text);
        try {
            return callDashscopeApi(prompt, null);
        } catch (Exception e) {
            log.error("Translation failed", e);
            return "翻译功能暂时不可用：" + e.getMessage();
        }
    }
    
    /**
     * Get conversation history
     */
    public List<AiChatResponse> getConversationHistory(Long userId) {
        return conversationHistory.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Clear conversation history
     */
    public void clearConversationHistory(Long userId) {
        conversationHistory.remove(userId);
    }
    
    /**
     * Call Dashscope API
     */
    private String callDashscopeApi(String userMessage, String context) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        
        // 构建消息
        List<Message> messages = new ArrayList<>();
        
        // 添加系统提示
        messages.add(Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的Markdown笔记助手，擅长帮助用户改进文字、总结内容、翻译文本和解释代码。请用简洁、准确的中文回答。")
                .build());
        
        // 添加上下文（如果有）
        if (StringUtils.hasText(context)) {
            messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content("上下文信息：" + context)
                    .build());
        }
        
        // 添加用户消息
        messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build());
        
        QwenParam param = QwenParam.builder()
                .model(model)
                .messages(messages)
                .topP(0.8)
                .temperature(0.7f)
                .maxTokens(2000)
                .build();
        
        GenerationResult result = gen.call(param);
        
        if (result != null && result.getOutput() != null && 
            result.getOutput().getChoices() != null && 
            !result.getOutput().getChoices().isEmpty()) {
            
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        }
        
        throw new RuntimeException("AI API returned empty response");
    }
}
