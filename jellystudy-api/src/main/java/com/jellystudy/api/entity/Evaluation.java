package com.jellystudy.api.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String targetId; // 问题ID或回答ID

    private String targetType; // question, answer

    private String extractedKnowledgePoint; // 大模型提取的知识点

    private String difficultyLevel; // easy, medium, hard

    private Integer score; // 0-100分

    private String evaluationReason; // 评估原因/说明

    private String modelVersion; // 使用的模型版本

    private LocalDateTime createTime;
}
