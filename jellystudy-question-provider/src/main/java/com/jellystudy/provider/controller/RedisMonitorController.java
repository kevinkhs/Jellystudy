package com.jellystudy.provider.controller;

import com.jellystudy.provider.service.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/redis")
public class RedisMonitorController {

    @Autowired
    private RedisCacheService redisCacheService;

    @GetMapping("/stats")
    public Map<String, Object> getCacheStatistics() {
        log.info("📊 获取Redis缓存统计信息");
        return redisCacheService.getCacheStatistics();
    }

    @GetMapping("/ranking")
    public Set<ZSetOperations.TypedTuple<String>> getRanking(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("🏆 获取问题排行榜 Top{}", limit);
        return redisCacheService.getTopQuestions(limit);
    }

    @GetMapping("/hot-questions")
    public List<String> getHotQuestions(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("🔥 获取热点问题列表 Top{}", limit);
        return redisCacheService.getHotQuestionIds(limit);
    }

    @GetMapping("/knowledge-points")
    public Map<String, Double> getKnowledgePointHeatMap() {
        log.info("📚 获取知识点热度图");
        return redisCacheService.getKnowledgePointHeatMap();
    }

    @GetMapping("/hot-knowledge-points")
    public Set<ZSetOperations.TypedTuple<String>> getHotKnowledgePoints(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("📈 获取热门知识点 Top{}", limit);
        return redisCacheService.getHotKnowledgePoints(limit);
    }

    @GetMapping("/view-count/{questionId}")
    public long getViewCount(@PathVariable String questionId) {
        long count = redisCacheService.getViewCountFromCache(questionId);
        log.info("👁️ 问题{}的浏览次数: {}", questionId, count);
        return count;
    }

    @GetMapping("/is-hot/{questionId}")
    public boolean isHotQuestion(@PathVariable String questionId) {
        boolean isHot = redisCacheService.isHotQuestion(questionId);
        log.info("🔥 问题{}是否为热点: {}", questionId, isHot ? "是" : "否");
        return isHot;
    }

    @DeleteMapping("/clear")
    public String clearAllCache() {
        log.warn("🧹 清除所有缓存...");
        redisCacheService.clearAllCache();
        return "✅ 所有JellyStudy缓存已清除";
    }

    @DeleteMapping("/question-cache/{questionId}")
    public String clearQuestionCache(@PathVariable String questionId) {
        log.info("🗑️ 清除问题缓存: {}", questionId);
        redisCacheService.invalidateQuestionCache(questionId);
        return "✅ 问题缓存已清除: " + questionId;
    }

    @PostMapping("/reset-knowledge-stats")
    public String resetKnowledgeStats() {
        log.warn("🔄 重置知识点统计...");
        redisCacheService.resetKnowledgePointStats();
        return "✅ 知识点热度统计已重置";
    }
}
