package com.jellystudy.web.controller;

import com.jellystudy.api.entity.KnowledgePoint;
import com.jellystudy.api.entity.Question;
import com.jellystudy.api.service.EvaluationService;
import com.jellystudy.api.service.KnowledgePointService;
import com.jellystudy.api.service.QuestionService;
import com.jellystudy.web.dto.AIRequestMessage;
import com.jellystudy.web.dto.AIResponseMessage;
import com.jellystudy.web.service.AIMessageProducer;
import com.jellystudy.web.service.AssistantCacheService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/assistant")
public class AssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AssistantController.class);

    @DubboReference(version = "1.0.0", group = "evaluation", timeout = 60000, check = false)
    private EvaluationService evaluationService;

    @DubboReference(version = "1.0.0", group = "knowledge-point", check = false)
    private KnowledgePointService knowledgePointService;

    @DubboReference(version = "1.0.0", group = "question", check = false)
    private QuestionService questionService;

    @Autowired
    private AssistantCacheService cacheService;

    @Autowired
    private AIMessageProducer messageProducer;

    @Autowired
    private Tracer tracer;

    @PostMapping("/chat")
    @ResponseBody
    public Map<String, Object> chat(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        Span span = tracer.nextSpan().name("assistant-chat").start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("action", "ai-chat");

            String message = request.get("message");
            String sessionId = getSessionId(httpRequest);

            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "消息不能为空");
                return response;
            }

            String requestId = UUID.randomUUID().toString();
            
            List<Map<String, String>> conversationHistory = cacheService.getChatHistory(sessionId);

            cacheService.addChatMessage(sessionId, 
                Map.of("role", "user", "content", message));

            AIRequestMessage aiRequest = AIRequestMessage.createChatRequest(
                requestId, sessionId, message, conversationHistory
            );

            logger.info("[TraceId: {}] 发送AI对话请求: requestId={}", 
                       span.context().traceId(), requestId);

            try {
                messageProducer.sendChatRequest(aiRequest);

                response.put("success", true);
                response.put("requestId", requestId);
                response.put("message", "请求已提交，正在处理中...");
                response.put("async", true);
            } catch (Exception e) {
                logger.error("[TraceId: {}] 发送AI请求失败", span.context().traceId(), e);
                
                String fallbackResponse = evaluationService.chatWithAI(message, conversationHistory);
                
                if (fallbackResponse != null) {
                    cacheService.addChatMessage(sessionId,
                        Map.of("role", "assistant", "content", fallbackResponse));
                    
                    response.put("success", true);
                    response.put("response", fallbackResponse);
                    response.put("fallback", true);
                    response.put("timestamp", System.currentTimeMillis());
                } else {
                    response.put("success", false);
                    response.put("error", "AI服务调用失败: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("AI对话异常", e);
            span.error(e);
            response.put("success", false);
            response.put("error", "对话失败: " + e.getMessage());
        } finally {
            span.end();
        }

        return response;
    }

    @GetMapping("/poll-response")
    @ResponseBody
    public Map<String, Object> pollResponse(@RequestParam String requestId,
                                          HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        String sessionId = getSessionId(httpRequest);

        try {
            AIResponseMessage aiResponse = cacheService.getAIResponse(sessionId, requestId);

            if (aiResponse != null) {
                response.put("success", true);
                response.put("completed", true);
                response.put("response", aiResponse.getResponse());
                response.put("graphData", aiResponse.getGraphData());
                response.put("error", aiResponse.getError());
                response.put("processingTime", aiResponse.getProcessingTimeMs());

                cacheService.removeAIResponse(sessionId, requestId);
            } else {
                response.put("success", true);
                response.put("completed", false);
                response.put("message", "仍在处理中...");
            }
        } catch (Exception e) {
            logger.error("轮询响应失败: requestId={}", requestId, e);
            response.put("success", false);
            response.put("error", "查询失败: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/mark-interest")
    @ResponseBody
    public Map<String, Object> markInterest(@RequestParam String knowledgePointId,
                                           @RequestParam String knowledgePointTitle,
                                           HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        Span span = tracer.nextSpan().name("mark-interest").start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("action", "mark-knowledge-point");
            span.tag("knowledgePointId", knowledgePointId);

            String sessionId = getSessionId(httpRequest);

            cacheService.addInterestedPoint(sessionId, Map.of(
                "id", knowledgePointId,
                "title", knowledgePointTitle,
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));

            List<Map<String, String>> points = cacheService.getInterestedPoints(sessionId);

            logger.info("[TraceId: {}] 标记知识点: id={}, title={}, total={}", 
                       span.context().traceId(), knowledgePointId, knowledgePointTitle, points.size());

            response.put("success", true);
            response.put("message", "已添加到兴趣列表");
            response.put("totalCount", points.size());
        } catch (Exception e) {
            logger.error("标记知识点失败", e);
            span.error(e);
            response.put("success", false);
            response.put("error", "标记失败: " + e.getMessage());
        } finally {
            span.end();
        }

        return response;
    }

    @PostMapping("/remove-interest")
    @ResponseBody
    public Map<String, Object> removeInterest(@RequestParam String knowledgePointId,
                                             HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        String sessionId = getSessionId(httpRequest);

        try {
            cacheService.removeInterestedPoint(sessionId, knowledgePointId);

            List<Map<String, String>> points = cacheService.getInterestedPoints(sessionId);

            logger.info("移除知识点: id={}", knowledgePointId);

            response.put("success", true);
            response.put("message", "已从列表移除");
            response.put("totalCount", points != null ? points.size() : 0);
        } catch (Exception e) {
            logger.error("移除知识点失败", e);
            response.put("success", false);
            response.put("error", "移除失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/get-interested-points")
    @ResponseBody
    public Map<String, Object> getInterestedPoints(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        String sessionId = getSessionId(httpRequest);

        try {
            List<Map<String, String>> points = cacheService.getInterestedPoints(sessionId);

            response.put("success", true);
            response.put("points", points != null ? points : new ArrayList<>());
            response.put("totalCount", points != null ? points.size() : 0);
        } catch (Exception e) {
            logger.error("获取兴趣列表失败", e);
            response.put("success", false);
            response.put("error", "获取失败: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/generate-graph")
    @ResponseBody
    public Map<String, Object> generateGraph(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        Span span = tracer.nextSpan().name("generate-graph").start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("action", "generate-knowledge-graph");

            String sessionId = getSessionId(httpRequest);
            List<Map<String, String>> interestedPoints = cacheService.getInterestedPoints(sessionId);

            if (interestedPoints == null || interestedPoints.isEmpty()) {
                response.put("success", false);
                response.put("error", "请先标记至少一个感兴趣的知识点");
                return response;
            }

            List<String> ids = new ArrayList<>();
            List<String> titles = new ArrayList<>();

            for (Map<String, String> point : interestedPoints) {
                ids.add(point.get("id"));
                titles.add(point.get("title"));
            }

            String requestId = UUID.randomUUID().toString();

            logger.info("[TraceId: {}] 生成知识图谱: pointsCount={}", 
                       span.context().traceId(), titles.size());

            AIRequestMessage graphRequest = AIRequestMessage.createGraphRequest(
                requestId, sessionId, ids, titles
            );

            try {
                messageProducer.sendGraphRequest(graphRequest);

                response.put("success", true);
                response.put("requestId", requestId);
                response.put("message", "图谱生成任务已提交...");
                response.put("async", true);
            } catch (Exception e) {
                logger.warn("[TraceId: {}] RabbitMQ不可用，使用同步模式", span.context().traceId());

                Map<String, Object> graphData = evaluationService.generateKnowledgeGraph(ids, titles);
                boolean success = graphData.containsKey("success") && (Boolean) graphData.get("success");

                if (success) {
                    response.put("success", true);
                    response.put("graph", graphData);
                    response.put("message", "知识图谱生成成功");
                } else {
                    response.put("success", false);
                    response.put("error", graphData.getOrDefault("error", "生成失败"));
                }
            }
        } catch (Exception e) {
            logger.error("生成知识图谱失败", e);
            span.error(e);
            response.put("success", false);
            response.put("error", "生成失败: " + e.getMessage());
        } finally {
            span.end();
        }

        return response;
    }

    @PostMapping("/clear-history")
    @ResponseBody
    public Map<String, Object> clearHistory(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        String sessionId = getSessionId(httpRequest);

        try {
            cacheService.clearAllData(sessionId);

            response.put("success", true);
            response.put("message", "已清除所有历史数据");
        } catch (Exception e) {
            logger.error("清除历史失败", e);
            response.put("success", false);
            response.put("error", "清除失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 记录知识点点击（浏览问题时自动调用）
     */
    @PostMapping("/record-kp-click")
    @ResponseBody
    public Map<String, Object> recordKnowledgePointClick(@RequestParam String kpId,
                                                          @RequestParam String kpTitle) {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = cacheService.recordKnowledgePointClick(kpId, kpTitle);
            response.put("success", true);
            response.put("clickCount", count);
        } catch (Exception e) {
            logger.error("记录知识点点击失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * 获取知识图谱数据（所有知识点点击统计）
     */
    @GetMapping("/graph-data")
    @ResponseBody
    public Map<String, Object> getGraphData() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> stats = cacheService.getKnowledgePointClickStats();
            response.put("success", true);
            response.put("nodes", stats);
        } catch (Exception e) {
            logger.error("获取图谱数据失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * 知识图谱可视化页面
     */
    @GetMapping("/knowledge-graph")
    public String knowledgeGraphPage() {
        return "assistant/knowledge-graph";
    }

    /**
     * 根据知识点点击频率推荐问题（点击越多排越前）
     */
    @GetMapping("/recommended-questions")
    @ResponseBody
    public Map<String, Object> getRecommendedQuestions(@RequestParam(defaultValue = "5") int limit,
                                                       @RequestParam(required = false) String excludeQuestionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> stats = cacheService.getKnowledgePointClickStats();
            List<Map<String, Object>> recommendations = new ArrayList<>();
            Set<String> addedQuestionIds = new HashSet<>();

            for (Map<String, Object> kpStat : stats) {
                if (recommendations.size() >= limit) break;
                String kpId = (String) kpStat.get("id");
                try {
                    List<Question> questions = questionService.getQuestionsByKnowledgePointId(kpId);
                    for (Question q : questions) {
                        if (recommendations.size() >= limit) break;
                        if (addedQuestionIds.contains(q.getId())) continue;
                        if (excludeQuestionId != null && excludeQuestionId.equals(q.getId())) continue;
                        addedQuestionIds.add(q.getId());
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", q.getId());
                        item.put("title", q.getTitle());
                        item.put("knowledgePointTitle", q.getKnowledgePointTitle());
                        item.put("knowledgePointId", q.getKnowledgePointId());
                        item.put("viewCount", q.getViewCount());
                        item.put("likeCount", q.getLikeCount());
                        item.put("answerCount", q.getAnswerCount());
                        item.put("kpClickCount", kpStat.get("clickCount"));
                        recommendations.add(item);
                    }
                } catch (Exception e) {
                    logger.warn("查询知识点{}的问题失败: {}", kpId, e.getMessage());
                }
            }

            response.put("success", true);
            response.put("recommendations", recommendations);
        } catch (Exception e) {
            logger.error("获取推荐问题失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    private String getSessionId(HttpServletRequest request) {
        String sessionId = (String) request.getSession().getAttribute("assistant-session-id");

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            request.getSession().setAttribute("assistant-session-id", sessionId);
            logger.info("创建新的会话ID: {}", sessionId);
        }

        return sessionId;
    }
}
