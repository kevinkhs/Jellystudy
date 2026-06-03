package com.jellystudy.provider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jellystudy.provider.config.NacosConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RefreshScope
public class RedisCacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    private static final String RANKING_KEY = "jelly:question:ranking";
    private static final String HOT_QUESTION_KEY = "jelly:question:hot";
    private static final String QUESTION_DETAIL_KEY = "jelly:question:detail:";
    private static final String KNOWLEDGE_HOT_KEY = "jelly:knowledge:hot";
    private static final String VIEW_COUNT_KEY = "jelly:question:view:";

    @PostConstruct
    public void init() {
        log.info("✅ Redis缓存服务初始化完成（Nacos配置中心已连接）");
        log.info("   - 排行榜Key: {}", RANKING_KEY);
        log.info("   - 热点问题Key: {}", HOT_QUESTION_KEY);
        log.info("   - 问题详情Key前缀: {}", QUESTION_DETAIL_KEY);
        log.info("   - 知识点热度Key: {}", KNOWLEDGE_HOT_KEY);
        log.info("📋 从Nacos读取的配置参数:");
        log.info("   - 问题详情TTL: {}秒", nacosConfigProperties.getRedis().getQuestionTtl());
        log.info("   - 热点问题TTL: {}秒", nacosConfigProperties.getRedis().getHotQuestionTtl());
        log.info("   - 排行榜大小限制: {}", nacosConfigProperties.getRedis().getRankingSize());
        log.info("   - 热点问题数量上限: {}", nacosConfigProperties.getRedis().getHotQuestionSize());
        log.info("   - Redis缓存启用状态: {}", nacosConfigProperties.getRedis().isEnabled());
    }

    // ==================== 功能1：最受欢迎问题排行榜 ====================

    public void incrementQuestionScore(String questionId, double score) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(RANKING_KEY, questionId, score);
            log.debug("📈 问题 {} 排行榜分数 +{}", questionId, score);
        } catch (Exception e) {
            log.error("❌ 更新排行榜分数失败: questionId={}", questionId, e);
        }
    }

    public void updateQuestionRanking(String questionId, int likeCount, int viewCount, int answerCount) {
        try {
            double score = calculatePopularityScore(likeCount, viewCount, answerCount);
            stringRedisTemplate.opsForZSet().add(RANKING_KEY, questionId, score);
            log.debug("🏆 更新问题排名: id={}, score={}", questionId, score);
        } catch (Exception e) {
            log.error("❌ 更新排行榜失败: questionId={}", questionId, e);
        }
    }

    private double calculatePopularityScore(int likeCount, int viewCount, int answerCount) {
        double likeWeight = 3.0;
        double viewWeight = 0.5;
        double answerWeight = 2.0;

        return (likeCount * likeWeight) +
               (viewCount * viewWeight) +
               (answerCount * answerWeight);
    }

    public Set<ZSetOperations.TypedTuple<String>> getTopQuestions(int limit) {
        try {
            Set<ZSetOperations.TypedTuple<String>> rankings =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

            if (rankings != null && !rankings.isEmpty()) {
                log.info("📊 获取Top{}热门问题", limit);
                rankings.forEach(r -> log.debug("   - 问题ID: {}, 分数: {}", r.getValue(), r.getScore()));
            }
            return rankings;
        } catch (Exception e) {
            log.error("❌ 获取排行榜失败", e);
            return Collections.emptySet();
        }
    }

    public List<String> getTopQuestionIds(int limit) {
        Set<ZSetOperations.TypedTuple<String>> topQuestions = getTopQuestions(limit);
        List<String> questionIds = new ArrayList<>();
        if (topQuestions != null) {
            topQuestions.forEach(t -> questionIds.add(t.getValue()));
        }
        return questionIds;
    }

    public long getRankingSize() {
        Long size = stringRedisTemplate.opsForZSet().size(RANKING_KEY);
        return size != null ? size : 0L;
    }

    public void removeQuestionFromRanking(String questionId) {
        stringRedisTemplate.opsForZSet().remove(RANKING_KEY, questionId);
        log.info("🗑️ 从排行榜移除问题: {}", questionId);
    }

    // ==================== 功能2：最常查看问题缓存 ====================

    public void recordQuestionView(String questionId) {
        try {
            String key = VIEW_COUNT_KEY + questionId;

            stringRedisTemplate.opsForValue().increment(key);

            long viewCount = Long.parseLong(stringRedisTemplate.opsForValue().get(key));

            if (viewCount == 1) {
                stringRedisTemplate.expire(key, Duration.ofHours(24));
                log.info("👁️ 首次记录问题浏览: {}", questionId);
            } else if (viewCount % 10 == 0) {
                addToHotQuestions(questionId, viewCount);
                log.debug("🔥 问题达到{}次浏览，加入热点列表: {}", viewCount, questionId);
            }

            incrementHotQuestionScore(questionId);
        } catch (Exception e) {
            log.error("❌ 记录问题浏览失败: questionId={}", questionId, e);
        }
    }

    public void cacheHotQuestion(String questionId, Object questionData, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(questionData);
            String key = HOT_QUESTION_KEY + ":" + questionId;

            stringRedisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
            log.info("💾 缓存热点问题: id={}, TTL={}s", questionId, ttlSeconds);
        } catch (Exception e) {
            log.error("❌ 缓存热点问题失败: questionId={}", questionId, e);
        }
    }

    public <T> T getHotQuestionCache(String questionId, Class<T> clazz) {
        try {
            String key = HOT_QUESTION_KEY + ":" + questionId;
            String json = stringRedisTemplate.opsForValue().get(key);

            if (json != null) {
                log.info("✅ 命中热点问题缓存: {}", questionId);
                return objectMapper.readValue(json, clazz);
            }
            return null;
        } catch (Exception e) {
            log.error("❌ 获取热点问题缓存失败: questionId={}", questionId, e);
            return null;
        }
    }

    public void incrementHotQuestionScore(String questionId) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(HOT_QUESTION_KEY, questionId, 1);
        } catch (Exception e) {
            log.error("❌ 更新热点问题分数失败", e);
        }
    }

    public void addToHotQuestions(String questionId, long viewCount) {
        try {
            stringRedisTemplate.opsForZSet().add(HOT_QUESTION_KEY, questionId, viewCount);

            Long size = stringRedisTemplate.opsForZSet().size(HOT_QUESTION_KEY);
            int maxSize = nacosConfigProperties.getRedis().getHotQuestionSize();
            if (size != null && size > maxSize) {
                stringRedisTemplate.opsForZSet().removeRange(HOT_QUESTION_KEY, 0, size - maxSize - 1);
                log.info("🧹 清理超出限制的热点问题，当前数量: {}，上限: {}", size, maxSize);
            }
        } catch (Exception e) {
            log.error("❌ 添加到热点列表失败", e);
        }
    }

    public List<String> getHotQuestionIds(int limit) {
        try {
            Set<String> hotQuestions = stringRedisTemplate.opsForZSet().reverseRange(HOT_QUESTION_KEY, 0, limit - 1);
            if (hotQuestions != null && !hotQuestions.isEmpty()) {
                log.info("🔥 获取Top{}热点问题", limit);
                return new ArrayList<>(hotQuestions);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("❌ 获取热点问题列表失败", e);
            return new ArrayList<>();
        }
    }

    public boolean isHotQuestion(String questionId) {
        Double rank = stringRedisTemplate.opsForZSet().score(HOT_QUESTION_KEY, questionId);
        return rank != null && rank >= 10;
    }

    public long getViewCountFromCache(String questionId) {
        try {
            String count = stringRedisTemplate.opsForValue().get(VIEW_COUNT_KEY + questionId);
            return count != null ? Long.parseLong(count) : 0L;
        } catch (Exception e) {
            log.error("❌ 获取浏览次数失败", e);
            return 0L;
        }
    }

    // ==================== 功能3：知识点热度统计（自定义场景）====================

    public void recordKnowledgePointAccess(String knowledgePointId) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(KNOWLEDGE_HOT_KEY, knowledgePointId, 1);
            log.debug("📚 记录知识点访问: {}", knowledgePointId);
        } catch (Exception e) {
            log.error("❌ 记录知识点访问失败", e);
        }
    }

    public void recordKnowledgePointQuestionCreated(String knowledgePointId) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(KNOWLEDGE_HOT_KEY, knowledgePointId, 5);
            log.info("📝 知识点新增问题: {} (+5分)", knowledgePointId);
        } catch (Exception e) {
            log.error("❌ 记录知识点问题创建失败", e);
        }
    }

    public Set<ZSetOperations.TypedTuple<String>> getHotKnowledgePoints(int limit) {
        try {
            Set<ZSetOperations.TypedTuple<String>> hotKps =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(KNOWLEDGE_HOT_KEY, 0, limit - 1);

            if (hotKps != null && !hotKps.isEmpty()) {
                log.info("📚 获取Top{}热门知识点", limit);
                hotKps.forEach(kp ->
                    log.debug("   - 知识点ID: {}, 热度: {}", kp.getValue(), kp.getScore())
                );
            }
            return hotKps;
        } catch (Exception e) {
            log.error("❌ 获取热门知识点失败", e);
            return Collections.emptySet();
        }
    }

    public Map<String, Double> getKnowledgePointHeatMap() {
        try {
            Set<ZSetOperations.TypedTuple<String>> allKps =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(KNOWLEDGE_HOT_KEY, 0, -1);

            Map<String, Double> heatMap = new LinkedHashMap<>();
            if (allKps != null) {
                allKps.forEach(kp -> heatMap.put(kp.getValue(), kp.getScore()));
            }
            return heatMap;
        } catch (Exception e) {
            log.error("❌ 获取知识点热度图失败", e);
            return Collections.emptyMap();
        }
    }

    public void resetKnowledgePointStats() {
        try {
            redisTemplate.delete(KNOWLEDGE_HOT_KEY);
            log.info("🔄 重置知识点热度统计");
        } catch (Exception e) {
            log.error("❌ 重置知识点统计失败", e);
        }
    }

    // ==================== 通用缓存操作 ====================

    public void cacheQuestionDetail(String questionId, Object detail, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(detail);
            String key = QUESTION_DETAIL_KEY + questionId;
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
            log.debug("💾 缓存问题详情: id={}, TTL={}s", questionId, ttlSeconds);
        } catch (Exception e) {
            log.error("❌ 缓存问题详情失败: questionId={}", questionId, e);
        }
    }

    public <T> T getCachedQuestionDetail(String questionId, Class<T> clazz) {
        try {
            String key = QUESTION_DETAIL_KEY + questionId;
            String json = stringRedisTemplate.opsForValue().get(key);

            if (json != null) {
                log.info("✅ 命中问题详情缓存: {}", questionId);
                return objectMapper.readValue(json, clazz);
            }
            return null;
        } catch (Exception e) {
            log.error("❌ 获取问题详情缓存失败: questionId={}", questionId, e);
            return null;
        }
    }

    public void invalidateQuestionCache(String questionId) {
        try {
            String key = QUESTION_DETAIL_KEY + questionId;
            redisTemplate.delete(key);
            log.info("🗑️ 清除问题缓存: {}", questionId);
        } catch (Exception e) {
            log.error("❌ 清除问题缓存失败: questionId={}", questionId, e);
        }
    }

    public void invalidateAllQuestionCaches() {
        try {
            Set<String> keys = redisTemplate.keys(QUESTION_DETAIL_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("🗑️ 清除所有问题缓存，共{}个", keys.size());
            }
        } catch (Exception e) {
            log.error("❌ 清除所有缓存失败", e);
        }
    }

    // ==================== 统计与监控 ====================

    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("ranking_size", getRankingSize());

        Long hotQuestionSize = stringRedisTemplate.opsForZSet().size(HOT_QUESTION_KEY);
        stats.put("hot_question_count", hotQuestionSize != null ? hotQuestionSize : 0);

        Long kpSize = stringRedisTemplate.opsForZSet().size(KNOWLEDGE_HOT_KEY);
        stats.put("knowledge_point_count", kpSize != null ? kpSize : 0);

        Set<String> detailKeys = redisTemplate.keys(QUESTION_DETAIL_KEY + "*");
        stats.put("cached_question_details", detailKeys != null ? detailKeys.size() : 0);

        Set<String> viewKeys = redisTemplate.keys(VIEW_COUNT_KEY + "*");
        stats.put("tracked_view_counts", viewKeys != null ? viewKeys.size() : 0);

        return stats;
    }

    public void clearAllCache() {
        try {
            Set<String> keys = redisTemplate.keys("jelly:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("🧹 清除所有JellyStudy缓存，共{}个key", keys.size());
            }
        } catch (Exception e) {
            log.error("❌ 清除全部缓存失败", e);
        }
    }
}
