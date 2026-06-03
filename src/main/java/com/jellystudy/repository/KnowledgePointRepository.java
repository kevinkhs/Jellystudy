package com.jellystudy.repository;

import com.jellystudy.entity.KnowledgePoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgePointRepository extends MongoRepository<KnowledgePoint, String> {
    List<KnowledgePoint> findByCategory(String category);

    List<KnowledgePoint> findByTitleContaining(String title);

    List<KnowledgePoint> findByQuestionCountGreaterThanOrderByQuestionCountDesc(Integer count);

    long countByCategory(String category);
}
