package com.jellystudy.web.service;

import com.jellystudy.api.service.EvaluationService;
import com.jellystudy.web.config.RabbitMQConfig;
import com.jellystudy.web.dto.AIRequestMessage;
import com.jellystudy.web.dto.AIResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AIMessageConsumer.class);

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AssistantCacheService cacheService;

    @RabbitListener(queues = RabbitMQConfig.AI_REQUEST_QUEUE)
    public void handleAIRequest(AIRequestMessage request) {
        logger.info("收到AI请求: type={}, requestId={}, sessionId={}", 
                   request.getType(), request.getRequestId(), request.getSessionId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            AIResponseMessage response;
            
            if ("CHAT".equals(request.getType())) {
                response = processChatRequest(request);
            } else if ("KNOWLEDGE_GRAPH".equals(request.getType())) {
                response = processGraphRequest(request);
            } else {
                throw new IllegalArgumentException("Unknown request type: " + request.getType());
            }
            
            response.setProcessingTime(request.getTimestamp());

            logger.info("保存AI响应到缓存: sessionId={}, requestId={}, success={}",
                       request.getSessionId(), request.getRequestId(), response.isSuccess());

            cacheService.saveAIResponse(request.getSessionId(), request.getRequestId(), response);

            logger.info("AI请求处理完成: requestId={}, processingTime={}ms, success={}", 
                       request.getRequestId(), response.getProcessingTimeMs(), response.isSuccess());
            
        } catch (Exception e) {
            logger.error("处理AI请求失败: requestId={}", request.getRequestId(), e);
            
            AIResponseMessage errorResponse = AIResponseMessage.createChatResponse(
                request.getRequestId(), false, null, e.getMessage()
            );
            errorResponse.setProcessingTime(request.getTimestamp());
            
            cacheService.saveAIResponse(request.getSessionId(), request.getRequestId(), errorResponse);
        }
    }

    private AIResponseMessage processChatRequest(AIRequestMessage request) {
        logger.debug("处理对话请求: messageLength={}", 
                    request.getUserMessage() != null ? request.getUserMessage().length() : 0);
        
        String aiResponse = evaluationService.chatWithAI(
            request.getUserMessage(),
            request.getConversationHistory()
        );
        
        if (aiResponse != null) {
            cacheService.addChatMessage(request.getSessionId(), 
                Map.of("role", "user", "content", request.getUserMessage()));
            cacheService.addChatMessage(request.getSessionId(),
                Map.of("role", "assistant", "content", aiResponse));
            
            return AIResponseMessage.createChatResponse(
                request.getRequestId(), true, aiResponse, null
            );
        } else {
            return AIResponseMessage.createChatResponse(
                request.getRequestId(), false, null, "AI服务返回空响应"
            );
        }
    }

    private AIResponseMessage processGraphRequest(AIRequestMessage request) {
        logger.debug("处理知识图谱请求: pointsCount={}", 
                    request.getKnowledgePointTitles() != null ? request.getKnowledgePointTitles().size() : 0);
        
        java.util.Map<String, Object> graphData = evaluationService.generateKnowledgeGraph(
            request.getKnowledgePointIds(),
            request.getKnowledgePointTitles()
        );
        
        boolean success = graphData.containsKey("success") && (Boolean) graphData.get("success");
        
        if (success) {
            return AIResponseMessage.createGraphResponse(
                request.getRequestId(), true, graphData, null
            );
        } else {
            return AIResponseMessage.createGraphResponse(
                request.getRequestId(), false, null,
                graphData.containsKey("error") ? graphData.get("error").toString() : "生成失败"
            );
        }
    }
}
