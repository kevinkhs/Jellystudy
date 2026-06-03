package com.jellystudy.api.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String content;

    private String author;

    private Integer likeCount = 0;

    private LocalDateTime createTime;
}
