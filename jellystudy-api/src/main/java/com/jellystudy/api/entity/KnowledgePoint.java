package com.jellystudy.api.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "knowledge_points")
public class KnowledgePoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String title;

    private String description;

    private String category;

    private Integer questionCount = 0;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
