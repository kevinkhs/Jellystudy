package com.jellystudy.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "likes")
@CompoundIndexes({
    @CompoundIndex(name = "target_idx", def = "{'targetType': 1, 'targetId': 1, 'userId': 1}")
})
public class Like {
    @Id
    private String id;

    private String targetType;

    private String targetId;

    private String userId;

    private LocalDateTime createTime;

    public static Like create(String targetType, String targetId, String userId) {
        Like like = new Like();
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        return like;
    }
}
