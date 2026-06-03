package com.jellystudy.controller;

import com.jellystudy.entity.KnowledgePoint;
import com.jellystudy.entity.Question;
import com.jellystudy.service.KnowledgePointService;
import com.jellystudy.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @GetMapping("/")
    public String home(Model model) {
        List<Question> latestQuestions = questionService.getRecommendedQuestions(0, 5);
        List<Question> hotQuestions = questionService.getHotQuestions();
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findHotKnowledgePoints();

        model.addAttribute("latestQuestions", latestQuestions);
        model.addAttribute("hotQuestions", hotQuestions);
        model.addAttribute("knowledgePoints", knowledgePoints);

        return "index";
    }
}
