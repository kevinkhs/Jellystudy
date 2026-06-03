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
@RequestMapping("/knowledge")
public class KnowledgePointController {

    @DubboReference(version = "1.0.0", group = "knowledge-point")
    private KnowledgePointService knowledgePointService;

    @DubboReference(version = "1.0.0", group = "question")
    private QuestionService questionService;

    @GetMapping("/list")
    public String list(Model model) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.getAllKnowledgePoints();
        model.addAttribute("knowledgePoints", knowledgePoints);
        return "knowledge/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "knowledge/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute KnowledgePoint knowledgePoint) {
        knowledgePointService.createKnowledgePoint(knowledgePoint);
        return "redirect:/knowledge/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        KnowledgePoint kp = knowledgePointService.getKnowledgePointById(id);
        List<Question> questions = questionService.getQuestionsByKnowledgePointId(id);
        model.addAttribute("knowledgePoint", kp);
        model.addAttribute("questions", questions);
        return "knowledge/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        KnowledgePoint kp = knowledgePointService.getKnowledgePointById(id);
        model.addAttribute("knowledgePoint", kp);
        return "knowledge/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable String id, @ModelAttribute KnowledgePoint knowledgePoint) {
        knowledgePointService.updateKnowledgePoint(id, knowledgePoint);
        return "redirect:/knowledge/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        knowledgePointService.deleteKnowledgePoint(id);
        return "redirect:/knowledge/list";
    }
}
