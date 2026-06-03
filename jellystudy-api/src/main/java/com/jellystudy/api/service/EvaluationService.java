package com.jellystudy.api.service;

import com.jellystudy.api.entity.Evaluation;
import java.util.List;
import java.util.Map;

public interface EvaluationService {

    Evaluation evaluateQuestion(String questionId, String questionTitle, String questionContent);

    Evaluation evaluateAnswer(String questionId, String answerId, String answerContent, String answerAuthor);

    String generateAnswer(String questionTitle, String questionContent);

    Evaluation getEvaluationById(String id);

    List<Evaluation> getEvaluationsByTargetId(String targetId);

    String chatWithAI(String userMessage, List<Map<String, String>> conversationHistory);

    Map<String, Object> generateKnowledgeGraph(List<String> knowledgePointIds, List<String> knowledgePointTitles);
}