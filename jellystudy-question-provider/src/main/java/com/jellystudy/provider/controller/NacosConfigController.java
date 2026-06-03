package com.jellystudy.provider.controller;

import com.jellystudy.provider.config.NacosConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nacos-config")
@RefreshScope
@EnableConfigurationProperties(NacosConfigProperties.class)
public class NacosConfigController {

    private final NacosConfigProperties nacosConfigProperties;
    private final ApplicationContext applicationContext;

    @Value("${jellystudy.feature.environment:development}")
    private String environment;

    @Value("${jellystudy.feature.ai-answer-enabled:true}")
    private boolean aiAnswerEnabled;

    @Value("${jellystudy.redis.question-ttl:3600}")
    private int questionTtl;

    public NacosConfigController(NacosConfigProperties nacosConfigProperties,
                                 ApplicationContext applicationContext) {
        this.nacosConfigProperties = nacosConfigProperties;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/all")
    public Map<String, Object> getAllConfigs() {
        log.info("📋 获取所有Nacos配置信息");

        Map<String, Object> config = new HashMap<>();
        config.put("timestamp", System.currentTimeMillis());
        config.put("environment", environment);

        Map<String, Object> redis = new HashMap<>();
        redis.put("questionTtl", questionTtl);
        redis.put("hotQuestionTtl", nacosConfigProperties.getRedis().getHotQuestionTtl());
        redis.put("rankingSize", nacosConfigProperties.getRedis().getRankingSize());
        redis.put("hotQuestionSize", nacosConfigProperties.getRedis().getHotQuestionSize());
        redis.put("enabled", nacosConfigProperties.getRedis().isEnabled());
        config.put("redis", redis);

        Map<String, Object> cache = new HashMap<>();
        cache.put("enabled", nacosConfigProperties.getCache().isEnabled());
        cache.put("strategy", nacosConfigProperties.getCache().getStrategy());
        cache.put("maxSize", nacosConfigProperties.getCache().getMaxSize());
        config.put("cache", cache);

        Map<String, Object> feature = new HashMap<>();
        feature.put("aiAnswerEnabled", aiAnswerEnabled);
        feature.put("rankingEnabled", nacosConfigProperties.getFeature().isRankingEnabled());
        feature.put("knowledgePointHotEnabled", nacosConfigProperties.getFeature().isKnowledgePointHotEnabled());
        feature.put("maxConcurrentRequests", nacosConfigProperties.getFeature().getMaxConcurrentRequests());
        config.put("feature", feature);

        return config;
    }

    @GetMapping("/redis")
    public Map<String, Object> getRedisConfig() {
        log.info("🔴 获取Redis缓存配置");
        Map<String, Object> redis = new HashMap<>();
        redis.put("questionTtl", questionTtl);
        redis.put("hotQuestionTtl", nacosConfigProperties.getRedis().getHotQuestionTtl());
        redis.put("rankingSize", nacosConfigProperties.getRedis().getRankingSize());
        redis.put("enabled", nacosConfigProperties.getRedis().isEnabled());
        return redis;
    }

    @GetMapping("/features")
    public Map<String, Object> getFeatures() {
        log.info("⚙️ 获取功能开关配置");
        Map<String, Object> features = new HashMap<>();
        features.put("environment", environment);
        features.put("aiAnswerEnabled", aiAnswerEnabled);
        features.put("rankingEnabled", nacosConfigProperties.getFeature().isRankingEnabled());
        features.put("knowledgePointHotEnabled", nacosConfigProperties.getFeature().isKnowledgePointHotEnabled());
        features.put("maxConcurrentRequests", nacosConfigProperties.getFeature().getMaxConcurrentRequests());
        return features;
    }

    @PostMapping("/refresh")
    public Map<String, Object> refreshConfig() {
        log.info("🔄 手动刷新Nacos配置...");

        try {
            applicationContext.publishEvent(
                new org.springframework.cloud.context.environment.EnvironmentChangeEvent(null)
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "配置刷新成功！");
            result.put("timestamp", System.currentTimeMillis());
            result.put("newEnvironment", environment);
            result.put("newAiAnswerEnabled", aiAnswerEnabled);
            result.put("newQuestionTtl", questionTtl);

            log.info("✅ 配置刷新完成 - Environment: {}, AI: {}, TTL: {}",
                    environment, aiAnswerEnabled, questionTtl);

            return result;
        } catch (Exception e) {
            log.error("❌ 配置刷新失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "配置刷新失败: " + e.getMessage());
            return error;
        }
    }
}