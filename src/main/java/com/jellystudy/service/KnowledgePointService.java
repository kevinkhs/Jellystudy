package com.jellystudy.service;

import com.jellystudy.entity.KnowledgePoint;
import com.jellystudy.repository.KnowledgePointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KnowledgePointService {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    public List<KnowledgePoint> findAll() {
        return knowledgePointRepository.findAll();
    }

    public KnowledgePoint findById(String id) {
        return knowledgePointRepository.findById(id).orElse(null);
    }

    public KnowledgePoint save(KnowledgePoint knowledgePoint) {
        return knowledgePointRepository.save(knowledgePoint);
    }

    public void deleteById(String id) {
        knowledgePointRepository.deleteById(id);
    }

    public KnowledgePoint update(String id, String title, String description, String category) {
        KnowledgePoint kp = findById(id);
        if (kp != null) {
            kp.setTitle(title);
            kp.setDescription(description);
            kp.setCategory(category);
            kp.setUpdateTime(java.time.LocalDateTime.now());
            return knowledgePointRepository.save(kp);
        }
        return null;
    }

    public List<KnowledgePoint> findByCategory(String category) {
        return knowledgePointRepository.findByCategory(category);
    }

    public List<KnowledgePoint> searchByTitle(String keyword) {
        return knowledgePointRepository.findByTitleContaining(keyword);
    }

    public List<KnowledgePoint> findHotKnowledgePoints() {
        return knowledgePointRepository.findByQuestionCountGreaterThanOrderByQuestionCountDesc(0);
    }
}
