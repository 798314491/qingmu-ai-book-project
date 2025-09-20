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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * Stream chat with AI
     */
    public void streamChat(AiChatRequest request, Long userId, SseEmitter emitter) {
        // 设置超时和错误处理
        emitter.onCompletion(() -> log.debug("SSE connection completed for user: {}", userId));
        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for user: {}", userId);
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("SSE connection error for user: {}", userId, ex);
            emitter.complete();
        });
        
        // 在异步执行之前验证用户权限
        if (userId == null || userId <= 0) {
            log.warn("Invalid user ID for stream chat: {}", userId);
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"无效的用户ID\"}"));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send error message", e);
                emitter.completeWithError(e);
            }
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                String response = callDashscopeApi(request.getMessage(), request.getContext());
                
                // 模拟流式输出
                String[] words = response.split("");
                for (int i = 0; i < words.length; i++) {
                    try {
                        // 检查连接是否还活跃
                        if (emitter == null) {
                            log.debug("SSE emitter is null, stopping stream");
                            break;
                        }
                        
                        // 发送流式数据
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data("{\"type\":\"content\",\"content\":\"" + escapeJson(words[i]) + "\"}"));
                        
                        // 添加小延迟模拟流式效果
                        Thread.sleep(50);
                    } catch (IOException e) {
                        log.error("Failed to send SSE data", e);
                        break;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Stream interrupted for user: {}", userId);
                        break;
                    } catch (Exception e) {
                        log.error("Unexpected error during streaming", e);
                        break;
                    }
                }
                
                // 发送完成信号
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"type\":\"done\"}"));
                } catch (IOException e) {
                    log.error("Failed to send completion signal", e);
                }
                
                // 保存到历史记录
                try {
                    AiChatResponse chatResponse = AiChatResponse.builder()
                            .id(UUID.randomUUID().toString())
                            .conversationId(request.getConversationId())
                            .message(request.getMessage())
                            .response(response)
                            .type(request.getType())
                            .timestamp(LocalDateTime.now())
                            .userId(userId)
                            .build();
                    
                    conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(chatResponse);
                } catch (Exception e) {
                    log.error("Failed to save conversation history", e);
                }
                
                // 完成连接
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Failed to complete SSE connection", e);
                }
                
            } catch (Exception e) {
                log.error("Stream chat failed for user: {}", userId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"type\":\"error\",\"error\":\"" + escapeJson(e.getMessage()) + "\"}"));
                } catch (IOException ioException) {
                    log.error("Failed to send error message", ioException);
                } finally {
                    try {
                        emitter.complete();
                    } catch (Exception completeException) {
                        log.error("Failed to complete SSE connection after error", completeException);
                    }
                }
            }
        });
    }
    
    /**
     * Stream enhance text
     */
    public void streamEnhanceText(String text, Long userId, SseEmitter emitter) {
        // 设置超时和错误处理
        emitter.onCompletion(() -> log.debug("SSE enhance connection completed for user: {}", userId));
        emitter.onTimeout(() -> {
            log.warn("SSE enhance connection timeout for user: {}", userId);
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("SSE enhance connection error for user: {}", userId, ex);
            emitter.complete();
        });
        
        // 在异步执行之前验证用户权限
        if (userId == null || userId <= 0) {
            log.warn("Invalid user ID for stream enhance: {}", userId);
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"无效的用户ID\"}"));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send error message", e);
                emitter.completeWithError(e);
            }
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                // 使用模拟润色响应
                String response = generateMockEnhanceResponse(text);
                
                // 模拟流式输出
                String[] words = response.split("");
                for (int i = 0; i < words.length; i++) {
                    try {
                        if (emitter == null) {
                            log.debug("SSE emitter is null, stopping enhance stream");
                            break;
                        }
                        
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data("{\"type\":\"content\",\"content\":\"" + escapeJson(words[i]) + "\"}"));
                        Thread.sleep(50);
                    } catch (IOException | InterruptedException e) {
                        log.error("Failed to send SSE enhance data", e);
                        break;
                    } catch (Exception e) {
                        log.error("Unexpected error during enhance streaming", e);
                        break;
                    }
                }
                
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"type\":\"done\"}"));
                } catch (IOException e) {
                    log.error("Failed to send enhance completion signal", e);
                }
                
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Failed to complete SSE enhance connection", e);
                }
                
            } catch (Exception e) {
                log.error("Stream enhance text failed for user: {}", userId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"type\":\"error\",\"error\":\"" + escapeJson(e.getMessage()) + "\"}"));
                } catch (IOException ioException) {
                    log.error("Failed to send enhance error message", ioException);
                } finally {
                    try {
                        emitter.complete();
                    } catch (Exception completeException) {
                        log.error("Failed to complete SSE enhance connection after error", completeException);
                    }
                }
            }
        });
    }
    
    /**
     * Stream summarize content
     */
    public void streamSummarizeContent(String content, Long userId, SseEmitter emitter) {
        // 设置超时和错误处理
        emitter.onCompletion(() -> log.debug("SSE summarize connection completed for user: {}", userId));
        emitter.onTimeout(() -> {
            log.warn("SSE summarize connection timeout for user: {}", userId);
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("SSE summarize connection error for user: {}", userId, ex);
            emitter.complete();
        });
        
        // 在异步执行之前验证用户权限
        if (userId == null || userId <= 0) {
            log.warn("Invalid user ID for stream summarize: {}", userId);
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"error\",\"error\":\"无效的用户ID\"}"));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send error message", e);
                emitter.completeWithError(e);
            }
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                // 使用模拟总结响应
                String response = generateMockSummarizeResponse(content);
                
                // 模拟流式输出
                String[] words = response.split("");
                for (int i = 0; i < words.length; i++) {
                    try {
                        if (emitter == null) {
                            log.debug("SSE emitter is null, stopping summarize stream");
                            break;
                        }
                        
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data("{\"type\":\"content\",\"content\":\"" + escapeJson(words[i]) + "\"}"));
                        Thread.sleep(50);
                    } catch (IOException | InterruptedException e) {
                        log.error("Failed to send SSE summarize data", e);
                        break;
                    } catch (Exception e) {
                        log.error("Unexpected error during summarize streaming", e);
                        break;
                    }
                }
                
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"type\":\"done\"}"));
                } catch (IOException e) {
                    log.error("Failed to send summarize completion signal", e);
                }
                
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Failed to complete SSE summarize connection", e);
                }
                
            } catch (Exception e) {
                log.error("Stream summarize content failed for user: {}", userId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"type\":\"error\",\"error\":\"" + escapeJson(e.getMessage()) + "\"}"));
                } catch (IOException ioException) {
                    log.error("Failed to send summarize error message", ioException);
                } finally {
                    try {
                        emitter.complete();
                    } catch (Exception completeException) {
                        log.error("Failed to complete SSE summarize connection after error", completeException);
                    }
                }
            }
        });
    }
    
    /**
     * Escape JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * 生成模拟AI响应
     */
    private String generateMockResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "您好！我是AI助手，有什么可以帮助您的吗？";
        }
        
        String message = userMessage.toLowerCase();
        
        if (message.contains("你好") || message.contains("介绍")) {
            return "你好！我是AI助手，很高兴为您服务。我可以帮助您处理各种文本内容，包括润色、总结、翻译等功能。有什么我可以帮助您的吗？";
        } else if (message.contains("润色")) {
            return "我可以帮您润色文字，让表达更加优雅和准确。请提供需要润色的文本，我会为您提供改进建议，包括语法优化、用词建议和表达方式改进。";
        } else if (message.contains("总结")) {
            return "我可以帮您总结内容的要点。请提供需要总结的文本，我会提取关键信息并整理成简洁的摘要，帮助您快速了解主要内容。";
        } else if (message.contains("翻译")) {
            return "我可以帮您翻译文本。支持多种语言之间的互译，包括中文、英文、日文、韩文等。请提供需要翻译的内容，我会为您提供准确的翻译结果。";
        } else if (message.contains("代码")) {
            return "我可以帮您解释和分析代码。请提供需要解释的代码片段，我会为您详细说明代码的功能、逻辑和实现原理，帮助您更好地理解代码。";
        } else if (message.contains("笑话") || message.contains("幽默")) {
            return "当然可以！这是一个经典的笑话：\n\n为什么数学书总是很忧郁？\n因为它有太多的问题。😄\n\n希望这个笑话能给您带来快乐！";
        } else {
            return "感谢您的消息：" + userMessage + "。我是AI助手，可以帮您处理各种文本任务。您可以让我润色文字、总结内容、翻译文本或解释代码。请告诉我您需要什么帮助！";
        }
    }
    
    /**
     * 生成模拟润色响应
     */
    private String generateMockEnhanceResponse(String text) {
        return "【润色后的文本】\n\n" + text + "\n\n【润色说明】\n我对原文进行了以下优化：\n1. 调整了句式结构，使表达更加流畅\n2. 优化了词汇选择，增强了表达的准确性\n3. 改进了语言风格，使其更加专业和优雅\n\n希望这个润色版本能更好地满足您的需求！";
    }
    
    /**
     * 生成模拟总结响应
     */
    private String generateMockSummarizeResponse(String content) {
        return "【内容总结】\n\n根据您提供的内容，我为您提取了以下关键要点：\n\n1. 主要内容：\n" + content.substring(0, Math.min(50, content.length())) + "...\n\n2. 核心观点：\n- 这是一个重要的主题\n- 需要重点关注的内容\n- 值得深入思考的问题\n\n3. 总结：\n以上内容涵盖了多个重要方面，建议您根据实际需要进行进一步的学习和思考。\n\n希望这个总结对您有帮助！";
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
                .apiKey(apiKey)
                .model(model)
                .messages(messages)
                .topP(0.8)
                .temperature(0.7f)
                .maxTokens(2000)
                .build();
        
        log.info("Calling DashScope API with model: {}", model);
        GenerationResult result = gen.call(param);
        
        return extractResponseContent(result);
    }
    
    /**
     * Extract content from GenerationResult with multiple fallback strategies
     */
    private String extractResponseContent(GenerationResult result) {
        if (result == null) {
            log.error("GenerationResult is null");
            throw new RuntimeException("AI API returned null result");
        }
        
        String requestId = result.getRequestId();
        log.info("Processing API Response - RequestId: {}", requestId);
        
        if (result.getOutput() == null) {
            log.error("GenerationResult.output is null for RequestId: {}", requestId);
            throw new RuntimeException("AI API returned null output");
        }
        
        Object output = result.getOutput();
        
        // 详细记录响应结构用于调试
        try {
            ObjectMapper mapper = new ObjectMapper();
            String outputJson = mapper.writeValueAsString(output);
            log.info("API Response Output JSON: {}", outputJson);
            
            // 记录output对象的类型和可用方法
            Class<?> outputClass = output.getClass();
            log.info("Output class: {}", outputClass.getName());
            log.debug("Available methods: {}", java.util.Arrays.toString(outputClass.getMethods()));
        } catch (Exception e) {
            log.warn("Failed to serialize API response for debugging: {}", e.getMessage());
        }
        
        // 策略1: 尝试从 choices 数组中获取内容（标准OpenAI格式）
        String content = tryExtractFromChoices(output, requestId);
        if (content != null) {
            return content;
        }
        
        // 策略2: 尝试从常见的文本字段获取内容
        content = tryExtractFromTextFields(output, requestId);
        if (content != null) {
            return content;
        }
        
        // 策略3: 使用反射尝试所有可能的getter方法
        content = tryExtractUsingReflection(output, requestId);
        if (content != null) {
            return content;
        }
        
        // 策略4: 如果有output但无法提取，返回调试信息
        log.error("API returned response but failed to extract content for RequestId: {}", requestId);
        return String.format("AI 响应成功但内容提取失败。RequestId: %s。请查看服务器日志获取详细调试信息。", requestId);
    }
    
    private String tryExtractFromChoices(Object output, String requestId) {
        try {
            java.lang.reflect.Method getChoicesMethod = output.getClass().getMethod("getChoices");
            Object choices = getChoicesMethod.invoke(output);
            
            if (choices != null && choices instanceof java.util.List) {
                java.util.List<?> choicesList = (java.util.List<?>) choices;
                if (!choicesList.isEmpty()) {
                    Object firstChoice = choicesList.get(0);
                    
                    // 尝试获取message.content
                    java.lang.reflect.Method getMessageMethod = firstChoice.getClass().getMethod("getMessage");
                    Object message = getMessageMethod.invoke(firstChoice);
                    
                    java.lang.reflect.Method getContentMethod = message.getClass().getMethod("getContent");
                    String content = (String) getContentMethod.invoke(message);
                    
                    if (StringUtils.hasText(content)) {
                        log.info("Successfully extracted content from choices[0].message.content for RequestId: {}", requestId);
                        return content;
                    }
                }
            }
            log.debug("Choices array is empty or null for RequestId: {}", requestId);
        } catch (Exception e) {
            log.debug("Failed to extract from choices array for RequestId: {}: {}", requestId, e.getMessage());
        }
        return null;
    }
    
    private String tryExtractFromTextFields(Object output, String requestId) {
        String[] possibleTextFields = {"getText", "getContent", "getResponse", "getResult", "getMessage"};
        
        for (String fieldName : possibleTextFields) {
            try {
                java.lang.reflect.Method method = output.getClass().getMethod(fieldName);
                Object result = method.invoke(output);
                if (result instanceof String && StringUtils.hasText((String) result)) {
                    log.info("Successfully extracted content from {} for RequestId: {}", fieldName, requestId);
                    return (String) result;
                }
            } catch (Exception e) {
                log.debug("Method {} not found or failed for RequestId: {}: {}", fieldName, requestId, e.getMessage());
            }
        }
        return null;
    }
    
    private String tryExtractUsingReflection(Object output, String requestId) {
        try {
            java.lang.reflect.Method[] methods = output.getClass().getMethods();
            for (java.lang.reflect.Method method : methods) {
                // 查找返回String类型且无参数的getter方法
                if (method.getReturnType() == String.class && 
                    method.getParameterCount() == 0 && 
                    method.getName().startsWith("get") &&
                    !method.getName().equals("getClass")) {
                    
                    try {
                        Object result = method.invoke(output);
                        if (result instanceof String && StringUtils.hasText((String) result)) {
                            String content = (String) result;
                            // 过滤掉一些明显不是内容的字段
                            if (!isMetadataField(method.getName()) && content.length() > 5) {
                                log.info("Extracted content using reflection from {} for RequestId: {}", method.getName(), requestId);
                                return content;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed to invoke {} for RequestId: {}: {}", method.getName(), requestId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Reflection extraction failed for RequestId: {}: {}", requestId, e.getMessage());
        }
        return null;
    }
    
    private boolean isMetadataField(String methodName) {
        String[] metadataFields = {"getId", "getRequestId", "getTimestamp", "getStatus", "getCode", "getError"};
        for (String field : metadataFields) {
            if (field.equals(methodName)) {
                return true;
            }
        }
        return false;
    }
}
