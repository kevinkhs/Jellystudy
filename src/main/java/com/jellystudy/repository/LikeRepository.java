package com.jellystudy.repository;

import com.jellystudy.entity.Like;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LikeRepository extends MongoRepository<Like, String> {
    List<Like> findByTargetTypeAndTargetId(String targetType, String targetId);

    boolean existsByTargetTypeAndTargetIdAndUserId(String targetType, String targetId, String userId);

    long countByTargetTypeAndTargetId(String targetType, String targetId);

    void deleteByTargetTypeAndTargetIdAndUserId(String targetType, String targetId, String userId);
}
