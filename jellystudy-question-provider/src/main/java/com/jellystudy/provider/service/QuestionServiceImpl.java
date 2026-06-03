package com.jellystudy.provider.service;

import com.jellystudy.api.entity.*;
import com.jellystudy.api.service.QuestionService;
import com.jellystudy.provider.repository.LikeRepository;
import com.jellystudy.provider.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@DubboService(version = "1.0.0", group = "question")
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RedisCacheService redisCacheService;
    @Value("${redis.cache.question-ttl:3600}")
    private long questionTtl;

    @Override
    public Question createQuestion(Question question) {
        question.setId(UUID.randomUUID().toString().replace("-", ""));
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        question.setViewCount(0);
        question.setLikeCount(0);
        question.setAnswerCount(0);
        question.setAnswers(new ArrayList<>());

        Question saved = questionRepository.save(question);

        if (question.getKnowledgePointId() != null) {
            redisCacheService.recordKnowledgePointQuestionCreated(question.getKnowledgePointId());
            log.info("📝 新问题创建，更新知识点热度: {}", question.getKnowledgePointId());
        }

        return saved;
    }

    @Override
    public Question updateQuestion(String id, Question question) {
        Question existing = getQuestionFromDB(id);

        existing.setTitle(question.getTitle());
        existing.setContent(question.getContent());
        existing.setUpdateTime(LocalDateTime.now());

        Question updated = questionRepository.save(existing);

        redisCacheService.invalidateQuestionCache(id);
        log.info("🔄 问题已更新，清除缓存: id={}", id);

        return updated;
    }

    @Override
    public void deleteQuestion(String id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("问题不存在");
        }
        questionRepository.deleteById(id);

        redisCacheService.removeQuestionFromRanking(id);
        redisCacheService.invalidateQuestionCache(id);
        log.info("🗑️ 问题已删除，清理Redis: id={}", id);
    }

    @Override
    public Question getQuestionById(String id) {
        Question cached = redisCacheService.getCachedQuestionDetail(id, Question.class);
        if (cached != null) {
            redisCacheService.recordQuestionView(id);
            if (cached.getKnowledgePointId() != null) {
                redisCacheService.recordKnowledgePointAccess(cached.getKnowledgePointId());
            }
            return cached;
        }

        Question question = getQuestionFromDB(id);
        if (question != null) {
            question.setViewCount(question.getViewCount() + 1);
            questionRepository.save(question);

            redisCacheService.recordQuestionView(id);
            redisCacheService.cacheQuestionDetail(id, question, questionTtl);
            redisCacheService.updateQuestionRanking(
                id,
                question.getLikeCount(),
                question.getViewCount(),
                question.getAnswerCount()
            );

            if (question.getKnowledgePointId() != null) {
                redisCacheService.recordKnowledgePointAccess(question.getKnowledgePointId());
            }

            log.info("✅ 获取问题详情（数据库）: id={}, 已缓存", id);
        }
        return question;
    }

    private Question getQuestionFromDB(String id) {
        return questionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> getRecommendedQuestions(int page, int size) {
        List<String> hotIds = redisCacheService.getHotQuestionIds(20);
        if (!hotIds.isEmpty()) {
            log.info("🔥 使用热点问题推荐（来自Redis），数量: {}", hotIds.size());

            List<Question> hotQuestions = new ArrayList<>();
            for (String id : hotIds) {
                try {
                    Question q = getQuestionById(id);
                    if (q != null) {
                        hotQuestions.add(q);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 获取热点问题失败: id={}", id);
                }
            }

            int start = page * size;
            int end = Math.min(start + size, hotQuestions.size());
            if (start < hotQuestions.size()) {
                return hotQuestions.subList(start, end);
            }
        }

        List<Question> all = questionRepository.findAllByOrderByCreateTimeDesc();
        int start = page * size;
        int end = Math.min(start + size, all.size());
        if (start >= all.size()) {
            return new ArrayList<>();
        }
        return all.subList(start, end);
    }

    @Override
    public List<Question> getHotQuestions() {
        List<String> topIds = redisCacheService.getTopQuestionIds(20);
        if (!topIds.isEmpty()) {
            log.info("🏆 使用Redis排行榜获取热门问题");

            List<Question> hotQuestions = new ArrayList<>();
            for (String id : topIds) {
                try {
                    Question q = getQuestionFromDB(id);
                    if (q != null) {
                        hotQuestions.add(q);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 获取排行问题失败: id={}", id);
                }
            }
            return hotQuestions;
        }

        log.info("📊 Redis无排行数据，使用数据库查询");
        return questionRepository.findAllByOrderByLikeCountDescViewCountDesc();
    }

    @Override
    public List<Question> getQuestionsByKnowledgePointId(String knowledgePointId) {
        redisCacheService.recordKnowledgePointAccess(knowledgePointId);
        return questionRepository.findByKnowledgePointId(knowledgePointId);
    }

    @Override
    public Answer addAnswer(String questionId, Answer answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        answer.setId(UUID.randomUUID().toString().replace("-", ""));
        answer.setCreateTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        answer.setLikeCount(0);
        answer.setComments(new ArrayList<>());

        if (question.getAnswers() == null) {
            question.setAnswers(new ArrayList<>());
        }
        question.getAnswers().add(answer);
        question.setAnswerCount(question.getAnswers().size());
        question.setUpdateTime(LocalDateTime.now());

        questionRepository.save(question);

        redisCacheService.updateQuestionRanking(
            questionId,
            question.getLikeCount(),
            question.getViewCount(),
            question.getAnswerCount()
        );
        redisCacheService.invalidateQuestionCache(questionId);

        log.info("💬 新增回答，更新排名: questionId={}", questionId);
        return answer;
    }

    @Override
    public Answer updateAnswer(String questionId, String answerId, Answer answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        if (question.getAnswers() != null) {
            for (Answer existing : question.getAnswers()) {
                if (existing.getId().equals(answerId)) {
                    existing.setContent(answer.getContent());
                    existing.setUpdateTime(LocalDateTime.now());
                    question.setUpdateTime(LocalDateTime.now());
                    questionRepository.save(question);
                    redisCacheService.invalidateQuestionCache(questionId);
                    return existing;
                }
            }
        }
        throw new RuntimeException("回答不存在");
    }

    @Override
    public void deleteAnswer(String questionId, String answerId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        if (question.getAnswers() != null) {
            question.getAnswers().removeIf(a -> a.getId().equals(answerId));
            question.setAnswerCount(question.getAnswers().size());
            question.setUpdateTime(LocalDateTime.now());
            questionRepository.save(question);

            redisCacheService.updateQuestionRanking(
                questionId,
                question.getLikeCount(),
                question.getViewCount(),
                question.getAnswerCount()
            );
            redisCacheService.invalidateQuestionCache(questionId);
        }
    }

    @Override
    public Comment addComment(String questionId, String answerId, Comment comment) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        comment.setId(UUID.randomUUID().toString().replace("-", ""));
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0);

        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerId)) {
                    if (answer.getComments() == null) {
                        answer.setComments(new ArrayList<>());
                    }
                    answer.getComments().add(comment);
                    question.setUpdateTime(LocalDateTime.now());
                    questionRepository.save(question);
                    redisCacheService.incrementQuestionScore(questionId, 0.5);
                    return comment;
                }
            }
        }
        throw new RuntimeException("回答不存在");
    }

    @Override
    public Comment updateComment(String questionId, String answerId, String commentId, Comment comment) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerId) && answer.getComments() != null) {
                    for (Comment existing : answer.getComments()) {
                        if (existing.getId().equals(commentId)) {
                            existing.setContent(comment.getContent());
                            question.setUpdateTime(LocalDateTime.now());
                            questionRepository.save(question);
                            return existing;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("评论不存在");
    }

    @Override
    public void deleteComment(String questionId, String answerId, String commentId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerId) && answer.getComments() != null) {
                    answer.getComments().removeIf(c -> c.getId().equals(commentId));
                    question.setUpdateTime(LocalDateTime.now());
                    questionRepository.save(question);
                    return;
                }
            }
        }
    }

    @Override
    public Like likeEntity(String userId, String targetType, String targetId) {
        Like existingLike = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (existingLike != null) {
            throw new RuntimeException("已经点赞过了");
        }

        Like likeEntity = new Like();
        likeEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        likeEntity.setUserId(userId);
        likeEntity.setTargetType(targetType);
        likeEntity.setTargetId(targetId);
        likeEntity.setCreateTime(LocalDateTime.now());
        likeRepository.save(likeEntity);

        if ("question".equals(targetType)) {
            Question question = questionRepository.findById(targetId).orElse(null);
            if (question != null) {
                question.setLikeCount(question.getLikeCount() + 1);
                questionRepository.save(question);

                redisCacheService.incrementQuestionScore(targetId, 3.0);
                redisCacheService.updateQuestionRanking(
                    targetId,
                    question.getLikeCount(),
                    question.getViewCount(),
                    question.getAnswerCount()
                );
                redisCacheService.invalidateQuestionCache(targetId);

                log.info("👍 问题点赞，更新排行榜: questionId={}, score=+3", targetId);
            }
        } else if ("answer".equals(targetType)) {
            List<Question> questions = questionRepository.findAll();
            for (Question q : questions) {
                if (q.getAnswers() != null) {
                    for (Answer a : q.getAnswers()) {
                        if (a.getId().equals(targetId)) {
                            a.setLikeCount(a.getLikeCount() + 1);
                            questionRepository.save(q);
                            redisCacheService.incrementQuestionScore(q.getId(), 2.0);
                            return likeEntity;
                        }
                    }
                }
            }
        }

        return likeEntity;
    }

    @Override
    public void unlikeEntity(String userId, String targetType, String targetId) {
        Like existingLike = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (existingLike == null) {
            throw new RuntimeException("未找到点赞记录");
        }

        likeRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

        if ("question".equals(targetType)) {
            Question question = questionRepository.findById(targetId).orElse(null);
            if (question != null && question.getLikeCount() > 0) {
                question.setLikeCount(question.getLikeCount() - 1);
                questionRepository.save(question);

                redisCacheService.incrementQuestionScore(targetId, -3.0);
                redisCacheService.updateQuestionRanking(
                    targetId,
                    question.getLikeCount(),
                    question.getViewCount(),
                    question.getAnswerCount()
                );
                redisCacheService.invalidateQuestionCache(targetId);

                log.info("👎 取消点赞，更新排行榜: questionId={}, score=-3", targetId);
            }
        } else if ("answer".equals(targetType)) {
            List<Question> questions = questionRepository.findAll();
            for (Question q : questions) {
                if (q.getAnswers() != null) {
                    for (Answer a : q.getAnswers()) {
                        if (a.getId().equals(targetId) && a.getLikeCount() > 0) {
                            a.setLikeCount(a.getLikeCount() - 1);
                            questionRepository.save(q);
                            redisCacheService.incrementQuestionScore(q.getId(), -2.0);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getTotalQuestionCount() {
        return (int) questionRepository.count();
    }

    @Override
    public int getTotalAnswerCount() {
        int total = 0;
        List<Question> questions = questionRepository.findAll();
        for (Question q : questions) {
            if (q.getAnswers() != null) {
                total += q.getAnswers().size();
            }
        }
        return total;
    }

    @Override
    public int getTotalCommentCount() {
        int total = 0;
        List<Question> questions = questionRepository.findAll();
        for (Question q : questions) {
            if (q.getAnswers() != null) {
                for (Answer a : q.getAnswers()) {
                    if (a.getComments() != null) {
                        total += a.getComments().size();
                    }
                }
            }
        }
        return total;
    }

    @Override
    public int getTotalLikeCount() {
        return (int) likeRepository.count();
    }

    @Override
    public List<Question> getTopLikedQuestions(int limit) {
        List<String> topIds = redisCacheService.getTopQuestionIds(limit);
        if (!topIds.isEmpty()) {
            log.info("🏆 从Redis排行榜获取Top{}点赞问题", limit);

            List<Question> topQuestions = new ArrayList<>();
            for (String id : topIds) {
                try {
                    Question q = getQuestionFromDB(id);
                    if (q != null) {
                        topQuestions.add(q);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 获取排行问题失败: id={}", id);
                }
            }
            return topQuestions;
        }

        log.info("📊 Redis无数据，使用数据库查询");
        List<Question> all = questionRepository.findAllByOrderByLikeCountDescViewCountDesc();
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    public List<Answer> getTopLikedAnswers(int limit) {
        List<Answer> allAnswers = new ArrayList<>();
        List<Question> questions = questionRepository.findAll();

        for (Question q : questions) {
            if (q.getAnswers() != null) {
                allAnswers.addAll(q.getAnswers());
            }
        }

        allAnswers.sort((a, b) -> b.getLikeCount() - a.getLikeCount());
        return allAnswers.size() > limit ? allAnswers.subList(0, limit) : allAnswers;
    }

    // ==================== 新增：Redis相关接口 ====================

    public Map<String, Object> getRedisStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.putAll(redisCacheService.getCacheStatistics());
        stats.put("db_question_count", getTotalQuestionCount());
        stats.put("db_answer_count", getTotalAnswerCount());
        stats.put("db_like_count", getTotalLikeCount());

        return stats;
    }

    public Set<ZSetOperations.TypedTuple<String>> getRankings(int limit) {
        return redisCacheService.getTopQuestions(limit);
    }

    public long getViewCountFromRedis(String questionId) {
        return redisCacheService.getViewCountFromCache(questionId);
    }

    public boolean isHotQuestion(String questionId) {
        return redisCacheService.isHotQuestion(questionId);
    }
}
