package com.jellystudy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private String id;

    private String content;

    private String author;

    private String parentId;

    private String path;

    private Integer likeCount = 0;

    private LocalDateTime createTime;

    public static Comment create(String content, String author, String parentId, String path) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setParentId(parentId);
        comment.setPath(path);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        return comment;
    }
}
