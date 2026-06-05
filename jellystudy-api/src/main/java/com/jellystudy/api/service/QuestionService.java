package com.jellystudy.api.service;

import com.jellystudy.api.entity.Answer;
import com.jellystudy.api.entity.Comment;
import com.jellystudy.api.entity.Like;
import com.jellystudy.api.entity.Question;
import java.util.List;

public interface QuestionService {

    Question createQuestion(Question question);

    Question updateQuestion(String id, Question question);

    void deleteQuestion(String id);

    Question getQuestionById(String id);

    List<Question> getAllQuestions();

    List<Question> getRecommendedQuestions(int page, int size);

    List<Question> getHotQuestions();

    List<Question> getQuestionsByKnowledgePointId(String knowledgePointId);

    Answer addAnswer(String questionId, Answer answer);

    Answer updateAnswer(String questionId, String answerId, Answer answer);

    void deleteAnswer(String questionId, String answerId);

    Comment addComment(String questionId, String answerId, Comment comment);

    Comment updateComment(String questionId, String answerId, String commentId, Comment comment);

    void deleteComment(String questionId, String answerId, String commentId);

    Like likeEntity(String userId, String targetType, String targetId);

    void unlikeEntity(String userId, String targetType, String targetId);

    int getTotalQuestionCount();

    int getTotalAnswerCount();

    int getTotalCommentCount();

    int getTotalLikeCount();

    int countByKnowledgePointId(String knowledgePointId);

    List<Question> getTopLikedQuestions(int limit);

    List<Answer> getTopLikedAnswers(int limit);
}
