package com.jellystudy.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "jellystudy")
public class NacosConfigProperties {

    private RedisConfig redis = new RedisConfig();
    private CacheConfig cache = new CacheConfig();
    private FeatureConfig feature = new FeatureConfig();

    @Data
    public static class RedisConfig {
        private int questionTtl = 3600;
        private int hotQuestionTtl = 1800;
        private int rankingSize = 20;
        private int hotQuestionSize = 50;
        private boolean enabled = true;
    }

    @Data
    public static class CacheConfig {
        private boolean enabled = true;
        private String strategy = "LRU";
        private int maxSize = 1000;
    }

    @Data
    public static class FeatureConfig {
        private boolean aiAnswerEnabled = true;
        private boolean rankingEnabled = true;
        private boolean knowledgePointHotEnabled = true;
        private String environment = "development";
        private int maxConcurrentRequests = 100;
    }
}