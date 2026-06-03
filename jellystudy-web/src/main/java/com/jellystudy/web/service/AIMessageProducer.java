package com.jellystudy.web.service;

import com.jellystudy.web.config.RabbitMQConfig;
import com.jellystudy.web.dto.AIRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(AIMessageProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendChatRequest(AIRequestMessage request) {
        try {
            logger.info("发送AI对话请求到队列: requestId={}, sessionId={}", 
                       request.getRequestId(), request.getSessionId());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AI_EXCHANGE,
                RabbitMQConfig.AI_ROUTING_KEY,
                request
            );
            
            logger.debug("AI请求已发送到队列");
        } catch (Exception e) {
            logger.error("发送AI请求失败", e);
            throw new RuntimeException("Failed to send AI request to queue", e);
        }
    }

    public void sendGraphRequest(AIRequestMessage request) {
        try {
            logger.info("发送知识图谱生成请求到队列: requestId={}", request.getRequestId());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AI_EXCHANGE,
                RabbitMQConfig.AI_ROUTING_KEY,
                request
            );
            
            logger.debug("图谱请求已发送到队列");
        } catch (Exception e) {
            logger.error("发送图谱请求失败", e);
            throw new RuntimeException("Failed to send graph request to queue", e);
        }
    }
}
