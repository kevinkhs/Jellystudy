package com.jellystudy.controller;

import com.jellystudy.entity.KnowledgePoint;
import com.jellystudy.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/knowledge")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @GetMapping("/list")
    public String list(Model model) {
        List<KnowledgePoint> list = knowledgePointService.findAll();
        model.addAttribute("knowledgePoints", list);
        return "knowledge/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "knowledge/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title, @RequestParam String description,
                         @RequestParam String category) {
        KnowledgePoint kp = KnowledgePoint.create(title, description, category);
        knowledgePointService.save(kp);
        return "redirect:/knowledge/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        KnowledgePoint kp = knowledgePointService.findById(id);
        model.addAttribute("knowledgePoint", kp);
        return "knowledge/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        KnowledgePoint kp = knowledgePointService.findById(id);
        model.addAttribute("knowledgePoint", kp);
        return "knowledge/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable String id, @RequestParam String title,
                         @RequestParam String description, @RequestParam String category) {
        knowledgePointService.update(id, title, description, category);
        return "redirect:/knowledge/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        knowledgePointService.deleteById(id);
        return "redirect:/knowledge/list";
    }
}
