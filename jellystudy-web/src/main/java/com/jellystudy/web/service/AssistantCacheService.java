package com.jellystudy.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jellystudy.web.dto.AIResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AssistantCacheService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantCacheService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CHAT_HISTORY_PREFIX = "assistant:chat:";
    private static final String INTEREST_PREFIX = "assistant:interest:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    public void saveChatHistory(String sessionId, List<Map<String, String>> history) {
        try {
            String key = CHAT_HISTORY_PREFIX + sessionId;
            String json = objectMapper.writeValueAsString(history);
            
            redisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            logger.debug("保存对话历史到Redis: sessionId={}, size={}", sessionId, history.size());
        } catch (JsonProcessingException e) {
            logger.error("序列化对话历史失败", e);
        }
    }

    public List<Map<String, String>> getChatHistory(String sessionId) {
        try {
            String key = CHAT_HISTORY_PREFIX + sessionId;
            String json = redisTemplate.opsForValue().get(key);
            
            if (json != null && !json.isEmpty()) {
                List<Map<String, String>> history = objectMapper.readValue(json, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                
                logger.debug("从Redis加载对话历史: sessionId={}, size={}", sessionId, history.size());
                return history;
            }
        } catch (Exception e) {
            logger.error("反序列化对话历史失败", e);
        }
        
        return new ArrayList<>();
    }

    public void addChatMessage(String sessionId, Map<String, String> message) {
        List<Map<String, String>> history = getChatHistory(sessionId);
        history.add(message);
        
        if (history.size() > 20) {
            history = history.subList(history.size() - 20, history.size());
        }
        
        saveChatHistory(sessionId, history);
    }

    public void clearChatHistory(String sessionId) {
        String key = CHAT_HISTORY_PREFIX + sessionId;
        redisTemplate.delete(key);
        logger.info("清除对话历史: sessionId={}", sessionId);
    }

    public void saveInterestedPoints(String sessionId, List<Map<String, String>> points) {
        try {
            String key = INTEREST_PREFIX + sessionId;
            String json = objectMapper.writeValueAsString(points);
            
            redisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS * 7, TimeUnit.HOURS);
            
            logger.debug("保存兴趣列表到Redis: sessionId={}, count={}", sessionId, points.size());
        } catch (JsonProcessingException e) {
            logger.error("序列化兴趣列表失败", e);
        }
    }

    public List<Map<String, String>> getInterestedPoints(String sessionId) {
        try {
            String key = INTEREST_PREFIX + sessionId;
            String json = redisTemplate.opsForValue().get(key);
            
            if (json != null && !json.isEmpty()) {
                List<Map<String, String>> points = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                
                logger.debug("从Redis加载兴趣列表: sessionId={}, count={}", sessionId, points.size());
                return points;
            }
        } catch (Exception e) {
            logger.error("反序列化兴趣列表失败", e);
        }
        
        return new ArrayList<>();
    }

    public void addInterestedPoint(String sessionId, Map<String, String> point) {
        List<Map<String, String>> points = getInterestedPoints(sessionId);
        
        boolean exists = points.stream()
            .anyMatch(p -> p.get("id").equals(point.get("id")));
        
        if (!exists) {
            points.add(point);
            saveInterestedPoints(sessionId, points);
            logger.info("添加兴趣知识点: sessionId={}, pointId={}", sessionId, point.get("id"));
        }
    }

    public void removeInterestedPoint(String sessionId, String pointId) {
        List<Map<String, String>> points = getInterestedPoints(sessionId);
        points.removeIf(p -> p.get("id").equals(pointId));
        saveInterestedPoints(sessionId, points);
        logger.info("移除兴趣知识点: sessionId={}, pointId={}", sessionId, pointId);
    }

    public void clearAllData(String sessionId) {
        clearChatHistory(sessionId);
        String key = INTEREST_PREFIX + sessionId;
        redisTemplate.delete(key);
        logger.info("清除所有缓存数据: sessionId={}", sessionId);
    }

    private static final String AI_RESPONSE_PREFIX = "assistant:response:";

    public void saveAIResponse(String sessionId, String requestId, AIResponseMessage response) {
        try {
            String key = AI_RESPONSE_PREFIX + sessionId + ":" + requestId;
            String json = objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(key, json, 1, TimeUnit.HOURS);

            logger.debug("保存AI响应到Redis: sessionId={}, requestId={}", sessionId, requestId);
        } catch (JsonProcessingException e) {
            logger.error("序列化AI响应失败", e);
        }
    }

    public AIResponseMessage getAIResponse(String sessionId, String requestId) {
        try {
            String key = AI_RESPONSE_PREFIX + sessionId + ":" + requestId;
            String json = redisTemplate.opsForValue().get(key);

            if (json != null && !json.isEmpty()) {
                AIResponseMessage response = objectMapper.readValue(json, AIResponseMessage.class);

                logger.debug("从Redis加载AI响应: sessionId={}, requestId={}", sessionId, requestId);
                return response;
            }
        } catch (Exception e) {
            logger.error("反序列化AI响应失败", e);
        }

        return null;
    }

    public boolean hasPendingRequest(String sessionId, String requestId) {
        String key = AI_RESPONSE_PREFIX + sessionId + ":" + requestId;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && !exists;
    }

    public void removeAIResponse(String sessionId, String requestId) {
        String key = AI_RESPONSE_PREFIX + sessionId + ":" + requestId;
        redisTemplate.delete(key);
    }
}
