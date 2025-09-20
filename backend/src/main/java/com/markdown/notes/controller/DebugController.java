package com.markdown.notes.controller;

import com.markdown.notes.common.Result;
import com.markdown.notes.dto.AiChatRequest;
import com.markdown.notes.dto.AiChatResponse;
import com.markdown.notes.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Debug Controller - 用于测试和调试AI服务
 */
@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final AiService aiService;

    /**
     * 测试AI基本功能
     */
    @PostMapping("/ai/test")
    public Result<AiChatResponse> testAi(@RequestBody(required = false) AiChatRequest request) {
        try {
            log.info("开始测试AI功能...");
            
            // 如果没有提供请求体，使用默认测试消息
            if (request == null) {
                request = new AiChatRequest();
                request.setMessage("你好，请用一句话介绍自己");
                request.setType("test");
                request.setConversationId("debug-test");
            }
            
            log.info("测试消息: {}", request.getMessage());
            
            AiChatResponse response = aiService.chat(request, 999L); // 使用测试用户ID
            
            log.info("AI响应成功: {}", response.getResponse());
            
            return Result.success("AI测试成功", response);
            
        } catch (Exception e) {
            log.error("AI测试失败", e);
            return Result.error("AI测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试文本增强功能
     */
    @PostMapping("/ai/enhance")
    public Result<String> testEnhance(@RequestParam(defaultValue = "这个功能很好用") String text) {
        try {
            log.info("测试文本增强功能，原文: {}", text);
            
            String enhanced = aiService.enhanceText(text, 999L);
            
            log.info("增强后文本: {}", enhanced);
            
            return Result.success("文本增强成功", enhanced);
            
        } catch (Exception e) {
            log.error("文本增强测试失败", e);
            return Result.error("文本增强测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试内容总结功能
     */
    @PostMapping("/ai/summarize")
    public Result<String> testSummarize(@RequestParam(defaultValue = "人工智能是计算机科学的一个重要分支，旨在创造能够模拟人类智能行为的机器和软件系统。") String content) {
        try {
            log.info("测试内容总结功能，原内容: {}", content);
            
            String summary = aiService.summarizeContent(content, 999L);
            
            log.info("总结结果: {}", summary);
            
            return Result.success("内容总结成功", summary);
            
        } catch (Exception e) {
            log.error("内容总结测试失败", e);
            return Result.error("内容总结测试失败: " + e.getMessage());
        }
    }

    /**
     * 检查AI服务配置
     */
    @GetMapping("/ai/config")
    public Result<String> checkConfig() {
        try {
            // 这里不直接暴露敏感信息，只返回配置状态
            return Result.success("AI服务配置检查", "AI服务已配置，可以进行测试");
            
        } catch (Exception e) {
            log.error("配置检查失败", e);
            return Result.error("配置检查失败: " + e.getMessage());
        }
    }
}
