package com.jellystudy.api.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Like implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String userId;

    private String targetType; // question, answer, comment

    private String targetId;

    private LocalDateTime createTime;
}
