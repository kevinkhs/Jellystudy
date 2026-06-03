package com.jellystudy.api.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "questions")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String title;

    private String content;

    private String author;

    private String knowledgePointId;

    private String knowledgePointTitle;

    private Integer viewCount = 0;

    private Integer likeCount = 0;

    private Integer answerCount = 0;

    private List<Answer> answers;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
