package com.jellystudy.provider.repository;

import com.jellystudy.api.entity.KnowledgePoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface KnowledgePointRepository extends MongoRepository<KnowledgePoint, String> {

    List<KnowledgePoint> findByCategory(String category);

    List<KnowledgePoint> findByTitleContainingIgnoreCase(String title);

    List<KnowledgePoint> findAllByOrderByQuestionCountDesc();
}
