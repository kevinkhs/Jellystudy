package com.jellystudy.web.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AIRequestMessage {

    private String requestId;

    private String sessionId;

    private String type;

    private String userMessage;

    private List<Map<String, String>> conversationHistory;

    private List<String> knowledgePointIds;

    private List<String> knowledgePointTitles;

    private long timestamp;

    public AIRequestMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public static AIRequestMessage createChatRequest(String requestId, String sessionId, 
                                                     String message, List<Map<String, String>> history) {
        AIRequestMessage request = new AIRequestMessage();
        request.setRequestId(requestId);
        request.setSessionId(sessionId);
        request.setType("CHAT");
        request.setUserMessage(message);
        request.setConversationHistory(history);
        return request;
    }

    public static AIRequestMessage createGraphRequest(String requestId, String sessionId,
                                                      List<String> ids, List<String> titles) {
        AIRequestMessage request = new AIRequestMessage();
        request.setRequestId(requestId);
        request.setSessionId(sessionId);
        request.setType("KNOWLEDGE_GRAPH");
        request.setKnowledgePointIds(ids);
        request.setKnowledgePointTitles(titles);
        return request;
    }
}
