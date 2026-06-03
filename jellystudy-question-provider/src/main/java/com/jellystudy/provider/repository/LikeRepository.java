package com.jellystudy.provider.repository;

import com.jellystudy.api.entity.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LikeRepository extends MongoRepository<Like, String> {

    Like findByUserIdAndTargetTypeAndTargetId(String userId, String targetType, String targetId);

    void deleteByUserIdAndTargetTypeAndTargetId(String userId, String targetType, String targetId);
}
