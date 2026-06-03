package com.jellystudy.service;

import com.jellystudy.entity.*;
import com.jellystudy.repository.LikeRepository;
import com.jellystudy.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private KnowledgePointService knowledgePointService;

    public Question createQuestion(String title, String content, String knowledgePointId, String author) {
        KnowledgePoint kp = knowledgePointService.findById(knowledgePointId);
        if (kp == null) {
            throw new RuntimeException("知识点不存在");
        }
        Question question = Question.create(title, content, knowledgePointId, kp.getTitle(), author);
        question = questionRepository.save(question);
        kp.setQuestionCount(kp.getQuestionCount() + 1);
        knowledgePointService.save(kp);
        return question;
    }

    public List<Question> findAllQuestions() {
        return questionRepository.findAll();
    }

    public Page<Question> findQuestionsByPage(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return questionRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    public Question findQuestionById(String id) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question != null) {
            question.setViewCount(question.getViewCount() + 1);
            questionRepository.save(question);
        }
        return question;
    }

    public Question updateQuestion(String id, String title, String content) {
        Question question = findById(id);
        if (question != null) {
            question.setTitle(title);
            question.setContent(content);
            question.setUpdateTime(java.time.LocalDateTime.now());
            return questionRepository.save(question);
        }
        return null;
    }

    public void deleteQuestion(String id) {
        Question question = findById(id);
        if (question != null && question.getAnswerCount() > 0) {
            throw new RuntimeException("该问题已有回答，无法删除");
        }
        questionRepository.deleteById(id);
        KnowledgePoint kp = knowledgePointService.findById(question.getKnowledgePointId());
        if (kp != null && kp.getQuestionCount() > 0) {
            kp.setQuestionCount(kp.getQuestionCount() - 1);
            knowledgePointService.save(kp);
        }
    }

    public Answer addAnswer(String questionId, String content, String author) {
        Question question = findById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }
        Answer answer = Answer.create(content, author);
        answer.setId(UUID.randomUUID().toString());
        question.getAnswers().add(answer);
        question.setAnswerCount(question.getAnswers().size());
        question.setUpdateTime(java.time.LocalDateTime.now());
        questionRepository.save(question);
        return answer;
    }

    public Answer updateAnswer(String questionId, String answerId, String content) {
        Question question = findById(questionId);
        if (question != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerId)) {
                    answer.setContent(content);
                    answer.setUpdateTime(java.time.LocalDateTime.now());
                    questionRepository.save(question);
                    return answer;
                }
            }
        }
        return null;
    }

    public void deleteAnswer(String questionId, String answerId) {
        Question question = findById(questionId);
        if (question != null) {
            question.getAnswers().removeIf(a -> a.getId().equals(answerId));
            question.setAnswerCount(question.getAnswers().size());
            question.setUpdateTime(java.time.LocalDateTime.now());
            questionRepository.save(question);
        }
    }

    public Comment addComment(String questionId, String answerId, String content, String author, String parentId) {
        Question question = findById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }
        Answer targetAnswer = null;
        for (Answer answer : question.getAnswers()) {
            if (answer.getId().equals(answerId)) {
                targetAnswer = answer;
                break;
            }
        }
        if (targetAnswer == null) {
            throw new RuntimeException("回答不存在");
        }
        String path;
        if (parentId == null || parentId.isEmpty()) {
            path = answerId;
        } else {
            Comment parentComment = findComment(targetAnswer, parentId);
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            path = parentComment.getPath() + "." + parentId;
        }
        Comment comment = Comment.create(content, author, parentId, path);
        comment.setId(UUID.randomUUID().toString());
        targetAnswer.getComments().add(comment);
        questionRepository.save(question);
        return comment;
    }

    private Comment findComment(Answer answer, String commentId) {
        for (Comment comment : answer.getComments()) {
            if (comment.getId().equals(commentId)) {
                return comment;
            }
        }
        return null;
    }

    public void updateComment(String questionId, String answerId, String commentId, String content) {
        Question question = findById(questionId);
        if (question != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerId)) {
                    for (Comment comment : answer.getComments()) {
                        if (comment.getId().equals(commentId)) {
                            comment.setContent(content);
                            questionRepository.save(question);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void deleteComment(String questionId, String answerId, String commentId) {
        Question question = findById(questionId);
        if (question != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerId)) {
                    answer.getComments().removeIf(c -> c.getId().equals(commentId) ||
                            (c.getPath() != null && c.getPath().contains(commentId)));
                    questionRepository.save(question);
                    return;
                }
            }
        }
    }

    public Like addLike(String targetType, String targetId, String userId) {
        if (likeRepository.existsByTargetTypeAndTargetIdAndUserId(targetType, targetId, userId)) {
            throw new RuntimeException("已经点赞过了");
        }
        Like like = Like.create(targetType, targetId, userId);
        like = likeRepository.save(like);
        updateLikeCount(targetType, targetId, 1);
        return like;
    }

    public void removeLike(String targetType, String targetId, String userId) {
        likeRepository.deleteByTargetTypeAndTargetIdAndUserId(targetType, targetId, userId);
        updateLikeCount(targetType, targetId, -1);
    }

    private void updateLikeCount(String targetType, String targetId, int delta) {
        if ("question".equals(targetType)) {
            Question question = questionRepository.findById(targetId).orElse(null);
            if (question != null) {
                question.setLikeCount(Math.max(0, question.getLikeCount() + delta));
                questionRepository.save(question);
            }
        } else if ("answer".equals(targetType)) {
            List<Question> questions = questionRepository.findAll();
            for (Question q : questions) {
                for (Answer a : q.getAnswers()) {
                    if (a.getId().equals(targetId)) {
                        a.setLikeCount(Math.max(0, a.getLikeCount() + delta));
                        questionRepository.save(q);
                        return;
                    }
                }
            }
        } else if ("comment".equals(targetType)) {
            List<Question> questions = questionRepository.findAll();
            for (Question q : questions) {
                for (Answer a : q.getAnswers()) {
                    for (Comment c : a.getComments()) {
                        if (c.getId().equals(targetId)) {
                            c.setLikeCount(Math.max(0, c.getLikeCount() + delta));
                            questionRepository.save(q);
                            return;
                        }
                    }
                }
            }
        }
    }

    public long getQuestionCount() {
        return questionRepository.count();
    }

    public List<Question> getHotQuestions() {
        return questionRepository.findTop10ByOrderByLikeCountDesc();
    }

    public List<Question> getMostViewedQuestions() {
        return questionRepository.findTop10ByOrderByViewCountDesc();
    }

    public List<Answer> getHighRatedAnswers() {
        List<Question> allQuestions = questionRepository.findAll();
        java.util.ArrayList<Answer> highRatedAnswers = new java.util.ArrayList<>();
        for (Question q : allQuestions) {
            for (Answer a : q.getAnswers()) {
                if (a.getLikeCount() >= 5) {
                    highRatedAnswers.add(a);
                }
            }
        }
        highRatedAnswers.sort((a, b) -> b.getLikeCount().compareTo(a.getLikeCount()));
        return highRatedAnswers.size() > 10 ? highRatedAnswers.subList(0, 10) : highRatedAnswers;
    }

    public List<Question> getRecommendedQuestions(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Question> result = questionRepository.findAllByOrderByCreateTimeDesc(pageable);
        return result.getContent();
    }

    public List<Question> getQuestionsByKnowledgePoint(String knowledgePointId) {
        return questionRepository.findByKnowledgePointId(knowledgePointId);
    }

    public Question getCompleteQuestionData(String id) {
        return findById(id);
    }

    public Question findById(String id) {
        return questionRepository.findById(id).orElse(null);
    }
}
