package com.jellystudy.provider.repository;

import com.jellystudy.api.entity.Answer;
import com.jellystudy.api.entity.Comment;
import com.jellystudy.api.entity.Like;
import com.jellystudy.api.entity.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findByKnowledgePointId(String knowledgePointId);

    List<Question> findAllByOrderByLikeCountDescViewCountDesc();

    List<Question> findAllByOrderByCreateTimeDesc();
}
