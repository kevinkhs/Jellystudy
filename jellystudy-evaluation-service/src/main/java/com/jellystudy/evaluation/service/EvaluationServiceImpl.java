package com.jellystudy.evaluation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jellystudy.api.entity.Evaluation;
import com.jellystudy.api.service.EvaluationService;
import com.jellystudy.evaluation.repository.EvaluationRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@DubboService(version = "1.0.0", group = "evaluation")
public class EvaluationServiceImpl implements EvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private EvaluationRepository evaluationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Evaluation evaluateQuestion(String questionId, String questionTitle, String questionContent) {
        logger.info("开始评估问题: {} - {}", questionId, questionTitle);

        try {
            String systemPrompt = "你是一个专业的知识问答评估专家。请对用户的问题进行智能分析。";
            
            StringBuilder userMessage = new StringBuilder();
            userMessage.append("请分析以下问题，并以JSON格式返回结果（只返回JSON，不要包含其他文字）：\n\n");
            userMessage.append("{\"extractedKnowledgePoint\":\"知识点\",\"difficultyLevel\":\"easy/medium/hard\",\"evaluationReason\":\"评估原因\"}\n\n");
            userMessage.append("问题标题：").append(questionTitle).append("\n");
            userMessage.append("问题内容：").append(questionContent);

            String response = deepSeekClient.chat(systemPrompt, userMessage.toString());
            logger.info("DeepSeek 问题评估原始响应: {}", response);

            if (response != null) {
                Evaluation evaluation = parseEvaluationResponse(response);
                evaluation.setTargetId(questionId);
                evaluation.setTargetType("question");
                evaluation.setModelVersion("deepseek-v4-pro");
                evaluation.setCreateTime(LocalDateTime.now());
                
                Evaluation saved = evaluationRepository.save(evaluation);
                logger.info("问题评估完成: questionId={}, knowledgePoint={}, difficulty={}", 
                           questionId, saved.getExtractedKnowledgePoint(), saved.getDifficultyLevel());
                return saved;
            }
        } catch (Exception e) {
            logger.error("评估问题失败: questionId={}", questionId, e);
        }

        return null;
    }

    @Override
    public Evaluation evaluateAnswer(String questionId, String answerId, 
                                    String answerContent, String answerAuthor) {
        logger.info("开始评估回答: questionId={}, answerId={}", questionId, answerId);

        try {
            String systemPrompt = "你是一个专业的知识问答评估专家。请对用户的回答进行质量评分。";
            
            StringBuilder userMessage = new StringBuilder();
            userMessage.append("请对以下回答进行评分，并以JSON格式返回结果（只返回JSON，不要包含其他文字）：\n\n");
            userMessage.append("{\"score\":85,\"evaluationReason\":\"评分理由\"}\n\n");
            userMessage.append("回答者：").append(answerAuthor).append("\n");
            userMessage.append("回答内容：").append(answerContent);

            String response = deepSeekClient.chat(systemPrompt, userMessage.toString());
            logger.info("DeepSeek 回答评估原始响应: {}", response);

            if (response != null) {
                Evaluation evaluation = parseAnswerEvaluationResponse(response);
                evaluation.setTargetId(answerId);
                evaluation.setTargetType("answer");
                evaluation.setModelVersion("deepseek-v4-pro");
                evaluation.setCreateTime(LocalDateTime.now());
                
                Evaluation saved = evaluationRepository.save(evaluation);
                logger.info("回答评估完成: answerId={}, score={}", answerId, saved.getScore());
                return saved;
            }
        } catch (Exception e) {
            logger.error("评估回答失败: answerId={}", answerId, e);
        }

        return null;
    }

    @Override
    public String generateAnswer(String questionTitle, String questionContent) {
        logger.info("开始生成AI回答: {} - {}", questionTitle, questionTitle);

        try {
            String systemPrompt = "你是一个名为'博识尊'的AI知识助手，擅长回答各种学术和技术问题。请用专业、详细、有条理的方式回答用户的问题。";
            
            StringBuilder userMessage = new StringBuilder();
            userMessage.append("请回答以下问题（直接给出回答内容，不要包含任何前缀或说明）：\n\n");
            userMessage.append("问题标题：").append(questionTitle).append("\n");
            userMessage.append("问题描述：").append(questionContent).append("\n\n");
            userMessage.append("要求：\n");
            userMessage.append("- 回答要专业、准确、有条理\n");
            userMessage.append("- 使用清晰的段落结构\n");
            userMessage.append("- 如有必要可以分点说明\n");
            userMessage.append("- 字数控制在200-500字之间");

            String response = deepSeekClient.chat(systemPrompt, userMessage.toString());
            logger.info("DeepSeek AI回答原始响应: {}", response);

            if (response != null && !response.trim().isEmpty()) {
                String cleanedResponse = cleanAiResponse(response);
                logger.info("AI回答生成成功，长度: {}", cleanedResponse.length());
                return cleanedResponse;
            }
        } catch (Exception e) {
            logger.error("生成AI回答失败: {}", e.getMessage(), e);
        }

        return null;
    }

    private String cleanAiResponse(String response) {
        if (response == null) return null;
        
        String cleaned = response.trim();
        
        if (cleaned.startsWith("\"")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        
        cleaned = cleaned.replace("\\n", "\n").replace("\\\"", "\"");
        
        if (cleaned.contains("{") && cleaned.contains("}")) {
            int jsonStart = cleaned.indexOf('{');
            int jsonEnd = cleaned.lastIndexOf('}');
            if (jsonEnd > jsonStart) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(cleaned.substring(jsonStart, jsonEnd + 1));
                    if (jsonNode.has("answer")) {
                        return jsonNode.get("answer").asText("");
                    }
                    if (jsonNode.has("content")) {
                        return jsonNode.get("content").asText("");
                    }
                    if (jsonNode.has("response")) {
                        return jsonNode.get("response").asText("");
                    }
                } catch (Exception e) {
                    logger.warn("尝试解析JSON格式的AI回答失败", e);
                }
            }
        }
        
        return cleaned;
    }

    @Override
    public Evaluation getEvaluationById(String id) {
        return evaluationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Evaluation> getEvaluationsByTargetId(String targetId) {
        return evaluationRepository.findByTargetId(targetId);
    }

    private Evaluation parseEvaluationResponse(String response) {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(UUID.randomUUID().toString().replace("-", ""));
        
        try {
            int jsonStart = response.indexOf('{');
            int jsonEnd = response.lastIndexOf('}');
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = response.substring(jsonStart, jsonEnd + 1).trim();
                logger.info("提取的JSON字符串: {}", jsonStr);
                
                JsonNode jsonNode = objectMapper.readTree(jsonStr);
                
                if (jsonNode.has("extractedKnowledgePoint")) {
                    String kp = jsonNode.get("extractedKnowledgePoint").asText("");
                    evaluation.setExtractedKnowledgePoint(kp);
                    logger.info("解析知识点: {}", kp);
                }
                
                if (jsonNode.has("difficultyLevel")) {
                    String diff = jsonNode.get("difficultyLevel").asText("");
                    evaluation.setDifficultyLevel(diff);
                    logger.info("解析难度: {}", diff);
                }
                
                if (jsonNode.has("evaluationReason")) {
                    String reason = jsonNode.get("evaluationReason").asText("");
                    evaluation.setEvaluationReason(reason);
                    logger.info("解析说明: {}", reason);
                }
            }
        } catch (Exception e) {
            logger.warn("使用Jackson解析失败，尝试手动解析。错误: {}", e.getMessage());
            evaluation.setEvaluationReason(response);
        }

        return evaluation;
    }

    private Evaluation parseAnswerEvaluationResponse(String response) {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(UUID.randomUUID().toString().replace("-", ""));

        try {
            int jsonStart = response.indexOf('{');
            int jsonEnd = response.lastIndexOf('}');

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = response.substring(jsonStart, jsonEnd + 1).trim();
                logger.info("提取的JSON字符串(回答): {}", jsonStr);

                JsonNode jsonNode = objectMapper.readTree(jsonStr);

                if (jsonNode.has("score")) {
                    int score = jsonNode.get("score").asInt(0);
                    evaluation.setScore(score);
                    logger.info("解析分数: {}", score);
                }

                if (jsonNode.has("evaluationReason")) {
                    String reason = jsonNode.get("evaluationReason").asText("");
                    evaluation.setEvaluationReason(reason);
                    logger.info("解析说明(回答): {}", reason);
                }
            }
        } catch (Exception e) {
            logger.warn("使用Jackson解析回答评估失败。错误: {}", e.getMessage());
            evaluation.setEvaluationReason(response);
        }

        return evaluation;
    }

    @Override
    public String chatWithAI(String userMessage, List<Map<String, String>> conversationHistory) {
        logger.info("开始AI对话: messageLength={}", userMessage != null ? userMessage.length() : 0);

        try {
            StringBuilder systemPromptBuilder = new StringBuilder();
            systemPromptBuilder.append("你是JellyStudy智能学习平台的AI助手，名字叫'博识尊'。");
            systemPromptBuilder.append("\n\n你的特点：");
            systemPromptBuilder.append("\n- 专业、友好、有耐心");
            systemPromptBuilder.append("\n- 擅长解答学术和技术问题");
            systemPromptBuilder.append("\n- 可以帮助用户理解知识点、分析问题、提供学习建议");
            systemPromptBuilder.append("\n- 回答要清晰、有条理，适当使用Markdown格式（列表、加粗等）");
            systemPromptBuilder.append("\n- 如果不确定，诚实地说'我不太确定'而不是编造答案");

            String systemPrompt = systemPromptBuilder.toString();

            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                List<Map<String, String>> recentHistory = conversationHistory.stream()
                        .skip(Math.max(0, conversationHistory.size() - 10))
                        .collect(Collectors.toList());

                StringBuilder contextMessage = new StringBuilder();
                contextMessage.append("以下是我们的对话历史（请参考上下文回答）：\n\n");
                for (Map<String, String> msg : recentHistory) {
                    String role = msg.getOrDefault("role", "user");
                    String content = msg.getOrDefault("content", "");
                    if ("user".equals(role)) {
                        contextMessage.append("用户：").append(content).append("\n");
                    } else if ("assistant".equals(role)) {
                        contextMessage.append("助手：").append(content).append("\n");
                    }
                }
                contextMessage.append("\n现在用户的新问题是：\n").append(userMessage);

                String response = deepSeekClient.chat(systemPrompt, contextMessage.toString());
                logger.info("AI对话响应长度: {}", response != null ? response.length() : 0);
                return response;
            } else {
                String response = deepSeekClient.chat(systemPrompt, userMessage);
                logger.info("AI对话响应长度（无历史）: {}", response != null ? response.length() : 0);
                return response;
            }
        } catch (Exception e) {
            logger.error("AI对话失败", e);
            return "抱歉，我遇到了一些问题，请稍后再试。错误信息：" + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> generateKnowledgeGraph(List<String> knowledgePointIds,
                                                      List<String> knowledgePointTitles) {
        logger.info("开始生成知识图谱: pointsCount={}",
                   knowledgePointTitles != null ? knowledgePointTitles.size() : 0);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            if (knowledgePointTitles == null || knowledgePointTitles.isEmpty()) {
                result.put("error", "知识点列表为空");
                return result;
            }

            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("你是一个知识图谱构建专家。根据给定的知识点列表，分析它们之间的关联关系。");
            systemPrompt.append("\n\n要求：");
            systemPrompt.append("\n1. 分析知识点之间的逻辑关系（如：依赖、包含、相关等）");
            systemPrompt.append("\n2. 返回JSON格式的图谱数据");
            systemPrompt.append("\n3. 关系类型包括：depends-on（依赖）、contains（包含）、related-to（相关）、prerequisite-of（前置）");

            StringBuilder userMessage = new StringBuilder();
            userMessage.append("请为以下知识点生成知识图谱数据。返回JSON格式：\n\n");
            userMessage.append("{\n");
            userMessage.append("  \"nodes\": [\n");
            userMessage.append("    {\"id\": \"1\", \"name\": \"知识点名称\", \"category\": \"分类\"}\n");
            userMessage.append("  ],\n");
            userMessage.append("  \"links\": [\n");
            userMessage.append("    {\"source\": \"1\", \"target\": \"2\", \"relation\": \"关系类型\", \"description\": \"描述\"}\n");
            userMessage.append("  ]\n");
            userMessage.append("}\n\n");
            userMessage.append("知识点列表：\n");
            for (int i = 0; i < knowledgePointTitles.size(); i++) {
                userMessage.append(String.format("%d. %s\n", i + 1, knowledgePointTitles.get(i)));
            }
            userMessage.append("\n请直接返回JSON，不要包含其他文字。");

            String response = deepSeekClient.chat(systemPrompt.toString(), userMessage.toString());
            logger.info("知识图谱原始响应: {}", response);

            if (response != null && !response.trim().isEmpty()) {
                int jsonStart = response.indexOf('{');
                int jsonEnd = response.lastIndexOf('}');

                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonStr = response.substring(jsonStart, jsonEnd + 1).trim();
                    JsonNode graphData = objectMapper.readTree(jsonStr);

                    List<Map<String, Object>> nodes = new ArrayList<>();
                    List<Map<String, Object>> links = new ArrayList<>();

                    if (graphData.has("nodes") && graphData.get("nodes").isArray()) {
                        ArrayNode nodesArray = (ArrayNode) graphData.get("nodes");
                        for (JsonNode node : nodesArray) {
                            Map<String, Object> nodeMap = new HashMap<>();
                            nodeMap.put("id", node.has("id") ? node.get("id").asText() : UUID.randomUUID().toString());
                            nodeMap.put("name", node.has("name") ? node.get("name").asText() : "未命名");
                            nodeMap.put("category", node.has("category") ? node.get("category").asText() : "默认");
                            nodes.add(nodeMap);
                        }
                    }

                    if (graphData.has("links") && graphData.get("links").isArray()) {
                        ArrayNode linksArray = (ArrayNode) graphData.get("links");
                        for (JsonNode link : linksArray) {
                            Map<String, Object> linkMap = new HashMap<>();
                            linkMap.put("source", link.has("source") ? link.get("source").asText() : "");
                            linkMap.put("target", link.has("target") ? link.get("target").asText() : "");
                            linkMap.put("relation", link.has("relation") ? link.get("relation").asText() : "related-to");
                            linkMap.put("description", link.has("description") ? link.get("description").asText() : "");
                            links.add(linkMap);
                        }
                    }

                    if (nodes.isEmpty()) {
                        for (int i = 0; i < knowledgePointTitles.size(); i++) {
                            Map<String, Object> nodeMap = new HashMap<>();
                            nodeMap.put("id", String.valueOf(i + 1));
                            nodeMap.put("name", knowledgePointTitles.get(i));
                            nodeMap.put("category", "默认");
                            nodes.add(nodeMap);
                        }

                        for (int i = 0; i < nodes.size() - 1; i++) {
                            Map<String, Object> linkMap = new HashMap<>();
                            linkMap.put("source", String.valueOf(i + 1));
                            linkMap.put("target", String.valueOf(i + 2));
                            linkMap.put("relation", "related-to");
                            linkMap.put("description", "相关联");
                            links.add(linkMap);
                        }
                    }

                    result.put("success", true);
                    result.put("nodes", nodes);
                    result.put("links", links);
                    result.put("knowledgePointIds", knowledgePointIds);
                    result.put("knowledgePointTitles", knowledgePointTitles);

                    logger.info("知识图谱生成成功: nodes={}, links={}", nodes.size(), links.size());
                } else {
                    result.put("error", "无法从响应中提取JSON数据");
                }
            } else {
                result.put("error", "AI返回空响应");
            }
        } catch (Exception e) {
            logger.error("生成知识图谱失败", e);
            result.put("error", "生成失败: " + e.getMessage());
        }

        return result;
    }
}