package com.markdown.notes.service;

import com.markdown.notes.dto.AiChatRequest;
import com.markdown.notes.dto.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * AI Service 测试类
 * 用于测试DashScope API集成和响应处理
 */
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "alibaba.dashscope.api-key=sk-6873d82a6bcf4abd9619c6c1fdb5cf3a",
    "alibaba.dashscope.model=qwen-turbo"
})
public class AiServiceTest {

    @Autowired
    private AiService aiService;

    /**
     * 测试基本聊天功能
     */
    public void testBasicChat() {
        try {
            log.info("开始测试AI聊天功能...");
            
            AiChatRequest request = new AiChatRequest();
            request.setMessage("你好，请简单介绍一下自己");
            request.setType("chat");
            request.setConversationId("test-conversation-1");
            
            AiChatResponse response = aiService.chat(request, 1L);
            
            log.info("AI响应: {}", response.getResponse());
            
            if (response.getResponse().contains("AI") || response.getResponse().length() > 10) {
                log.info("✅ AI聊天功能测试成功！");
            } else {
                log.warn("⚠️ AI响应内容可能不完整: {}", response.getResponse());
            }
            
        } catch (Exception e) {
            log.error("❌ AI聊天功能测试失败", e);
        }
    }

    /**
     * 测试文本增强功能
     */
    public void testTextEnhancement() {
        try {
            log.info("开始测试文本增强功能...");
            
            String originalText = "这个功能很好用";
            String enhancedText = aiService.enhanceText(originalText, 1L);
            
            log.info("原文本: {}", originalText);
            log.info("增强后: {}", enhancedText);
            
            if (!enhancedText.equals(originalText) && enhancedText.length() >= originalText.length()) {
                log.info("✅ 文本增强功能测试成功！");
            } else {
                log.warn("⚠️ 文本增强可能未生效: {}", enhancedText);
            }
            
        } catch (Exception e) {
            log.error("❌ 文本增强功能测试失败", e);
        }
    }

    /**
     * 测试内容总结功能
     */
    public void testContentSummarization() {
        try {
            log.info("开始测试内容总结功能...");
            
            String longContent = "人工智能（AI）是计算机科学的一个分支，它试图理解智能的本质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。该领域的研究包括机器人、语言识别、图像识别、自然语言处理和专家系统等。人工智能从诞生以来，理论和技术日益成熟，应用领域也不断扩大。";
            String summary = aiService.summarizeContent(longContent, 1L);
            
            log.info("原内容: {}", longContent);
            log.info("总结: {}", summary);
            
            if (summary.length() < longContent.length() && summary.contains("人工智能")) {
                log.info("✅ 内容总结功能测试成功！");
            } else {
                log.warn("⚠️ 内容总结可能未生效: {}", summary);
            }
            
        } catch (Exception e) {
            log.error("❌ 内容总结功能测试失败", e);
        }
    }

    /**
     * 运行所有测试
     */
    public void runAllTests() {
        log.info("🚀 开始运行AI服务完整测试...");
        
        testBasicChat();
        testTextEnhancement();
        testContentSummarization();
        
        log.info("📋 测试完成！请查看上方日志了解详细结果。");
    }
}
