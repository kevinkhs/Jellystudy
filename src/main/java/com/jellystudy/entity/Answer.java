package com.jellystudy.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Answer {
    private String id;

    private String content;

    private String author;

    private Integer likeCount = 0;

    private Integer score;

    private Boolean isAccepted = false;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<Comment> comments = new ArrayList<>();

    public static Answer create(String content, String author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setAuthor(author);
        answer.setLikeCount(0);
        answer.setIsAccepted(false);
        answer.setCreateTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        return answer;
    }
}
