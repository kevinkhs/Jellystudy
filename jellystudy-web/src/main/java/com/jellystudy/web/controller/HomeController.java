package com.jellystudy.web.controller;

import com.jellystudy.api.entity.KnowledgePoint;
import com.jellystudy.api.entity.Question;
import com.jellystudy.api.service.KnowledgePointService;
import com.jellystudy.api.service.QuestionService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class HomeController {

    @DubboReference(version = "1.0.0", group = "knowledge-point")
    private KnowledgePointService knowledgePointService;

    @DubboReference(version = "1.0.0", group = "question")
    private QuestionService questionService;

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
