package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.KnowledgePointService;
import com.jellystudy.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Question> questions = questionService.findQuestionsByPage(page, 10);
        model.addAttribute("questions", questions.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        return "question/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findAll();
        model.addAttribute("knowledgePoints", knowledgePoints);
        return "question/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title, @RequestParam String content,
                         @RequestParam String knowledgePointId, @RequestParam String author) {
        Question question = questionService.createQuestion(title, content, knowledgePointId, author);
        return "redirect:/question/" + question.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        Question question = questionService.getCompleteQuestionData(id);
        model.addAttribute("question", question);
        return "question/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        Question question = questionService.findById(id);
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findAll();
        model.addAttribute("question", question);
        model.addAttribute("knowledgePoints", knowledgePoints);
        return "question/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable String id, @RequestParam String title,
                         @RequestParam String content) {
        questionService.updateQuestion(id, title, content);
        return "redirect:/question/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        try {
            questionService.deleteQuestion(id);
            return "redirect:/question/list";
        } catch (Exception e) {
            return "redirect:/question/" + id + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}/answer")
    public String addAnswer(@PathVariable String id, @RequestParam String content,
                            @RequestParam String author) {
        questionService.addAnswer(id, content, author);
        return "redirect:/question/" + id;
    }

    @PostMapping("/{questionId}/answer/{answerId}/update")
    public String updateAnswer(@PathVariable String questionId, @PathVariable String answerId,
                              @RequestParam String content) {
        questionService.updateAnswer(questionId, answerId, content);
        return "redirect:/question/" + questionId;
    }

    @GetMapping("/{questionId}/answer/{answerId}/delete")
    public String deleteAnswer(@PathVariable String questionId, @PathVariable String answerId) {
        questionService.deleteAnswer(questionId, answerId);
        return "redirect:/question/" + questionId;
    }

    @PostMapping("/{questionId}/answer/{answerId}/comment")
    public String addComment(@PathVariable String questionId, @PathVariable String answerId,
                             @RequestParam String content, @RequestParam String author,
                             @RequestParam(required = false) String parentId) {
        questionService.addComment(questionId, answerId, content, author, parentId);
        return "redirect:/question/" + questionId;
    }

    @PostMapping("/{questionId}/answer/{answerId}/comment/{commentId}/update")
    public String updateComment(@PathVariable String questionId, @PathVariable String answerId,
                                @PathVariable String commentId, @RequestParam String content) {
        questionService.updateComment(questionId, answerId, commentId, content);
        return "redirect:/question/" + questionId;
    }

    @GetMapping("/{questionId}/answer/{answerId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable String questionId, @PathVariable String answerId,
                                @PathVariable String commentId) {
        questionService.deleteComment(questionId, answerId, commentId);
        return "redirect:/question/" + questionId;
    }

    @PostMapping("/{id}/like")
    public String addLike(@PathVariable String id, @RequestParam String userId) {
        questionService.addLike("question", id, userId);
        return "redirect:/question/" + id;
    }

    @GetMapping("/{id}/unlike")
    public String removeLike(@PathVariable String id, @RequestParam String userId) {
        questionService.removeLike("question", id, userId);
        return "redirect:/question/" + id;
    }

    @PostMapping("/{questionId}/answer/{answerId}/like")
    public String likeAnswer(@PathVariable String questionId, @PathVariable String answerId,
                             @RequestParam String userId) {
        questionService.addLike("answer", answerId, userId);
        return "redirect:/question/" + questionId;
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuestions", questionService.getQuestionCount());
        stats.put("hotQuestions", questionService.getHotQuestions());
        stats.put("mostViewedQuestions", questionService.getMostViewedQuestions());
        stats.put("highRatedAnswers", questionService.getHighRatedAnswers());
        model.addAttribute("stats", stats);
        return "question/statistics";
    }

    @GetMapping("/recommended")
    public String recommended(@RequestParam(defaultValue = "0") int page, Model model) {
        List<Question> questions = questionService.getRecommendedQuestions(page, 10);
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        return "question/recommended";
    }

    @GetMapping("/knowledge-point/{kpId}")
    public String byKnowledgePoint(@PathVariable String kpId, Model model) {
        List<Question> questions = questionService.getQuestionsByKnowledgePoint(kpId);
        KnowledgePoint kp = knowledgePointService.findById(kpId);
        model.addAttribute("questions", questions);
        model.addAttribute("knowledgePoint", kp);
        return "question/by-knowledge-point";
    }
}
