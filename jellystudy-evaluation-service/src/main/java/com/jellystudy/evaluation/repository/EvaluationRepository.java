package com.jellystudy.evaluation.repository;

import com.jellystudy.api.entity.Evaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EvaluationRepository extends MongoRepository<Evaluation, String> {

    List<Evaluation> findByTargetId(String targetId);

    List<Evaluation> findByTargetType(String targetType);
}
