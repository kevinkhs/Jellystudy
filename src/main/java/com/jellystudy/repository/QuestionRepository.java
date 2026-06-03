package com.jellystudy.repository;

import com.jellystudy.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findByKnowledgePointId(String knowledgePointId);

    List<Question> findByAuthor(String author);

    List<Question> findByDifficulty(String difficulty);

    Page<Question> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<Question> findByKnowledgePointIdOrderByCreateTimeDesc(String knowledgePointId, Pageable pageable);

    @Query(sort = "{likeCount: -1}")
    List<Question> findTop10ByOrderByLikeCountDesc();

    @Query(sort = "{viewCount: -1}")
    List<Question> findTop10ByOrderByViewCountDesc();

    @Query(value = "{'answers': {$size: 0}}")
    List<Question> findUnansweredQuestions();

    long countByKnowledgePointId(String knowledgePointId);

    long count();
}
