package com.jellystudy.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "knowledge_points")
public class KnowledgePoint {
    @Id
    private String id;

    @Indexed
    private String title;

    private String description;

    private String category;

    @Indexed
    private Integer questionCount = 0;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static KnowledgePoint create(String title, String description, String category) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setTitle(title);
        kp.setDescription(description);
        kp.setCategory(category);
        kp.setQuestionCount(0);
        kp.setCreateTime(LocalDateTime.now());
        kp.setUpdateTime(LocalDateTime.now());
        return kp;
    }
}
