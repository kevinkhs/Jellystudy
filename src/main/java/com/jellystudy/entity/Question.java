package com.jellystudy.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "questions")
public class Question {
    @Id
    private String id;

    private String title;

    private String content;

    @Indexed
    private String knowledgePointId;

    private String knowledgePointTitle;

    @Indexed
    private String author;

    private Integer viewCount = 0;

    private Integer likeCount = 0;

    private Integer answerCount = 0;

    private String difficulty;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<Answer> answers = new ArrayList<>();

    public static Question create(String title, String content, String knowledgePointId,
                                  String knowledgePointTitle, String author) {
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setKnowledgePointId(knowledgePointId);
        question.setKnowledgePointTitle(knowledgePointTitle);
        question.setAuthor(author);
        question.setViewCount(0);
        question.setLikeCount(0);
        question.setAnswerCount(0);
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        return question;
    }
}
