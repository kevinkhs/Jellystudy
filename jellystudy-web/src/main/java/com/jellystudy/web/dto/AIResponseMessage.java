package com.jellystudy.web.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AIResponseMessage {

    private String requestId;

    private String sessionId;

    private String type;

    private boolean success;

    private String response;

    private Map<String, Object> graphData;

    private String error;

    private long timestamp;
    private long processingTimeMs;

    public AIResponseMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public static AIResponseMessage createChatResponse(String requestId, boolean success,
                                                       String response, String error) {
        AIResponseMessage resp = new AIResponseMessage();
        resp.setRequestId(requestId);
        resp.setType("CHAT");
        resp.setSuccess(success);
        resp.setResponse(response);
        resp.setError(error);
        return resp;
    }

    public static AIResponseMessage createGraphResponse(String requestId, boolean success,
                                                        Map<String, Object> graphData, String error) {
        AIResponseMessage resp = new AIResponseMessage();
        resp.setRequestId(requestId);
        resp.setType("KNOWLEDGE_GRAPH");
        resp.setSuccess(success);
        resp.setGraphData(graphData);
        resp.setError(error);
        return resp;
    }

    public void setProcessingTime(long requestTimestamp) {
        this.processingTimeMs = System.currentTimeMillis() - requestTimestamp;
    }
}
