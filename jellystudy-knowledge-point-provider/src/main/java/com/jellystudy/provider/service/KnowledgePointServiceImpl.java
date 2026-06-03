package com.jellystudy.provider.service;

import com.jellystudy.api.entity.KnowledgePoint;
import com.jellystudy.api.service.KnowledgePointService;
import com.jellystudy.provider.repository.KnowledgePointRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;

@DubboService(version = "1.0.0", group = "knowledge-point")
public class KnowledgePointServiceImpl implements KnowledgePointService {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Override
    public KnowledgePoint createKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePoint.setCreateTime(LocalDateTime.now());
        knowledgePoint.setUpdateTime(LocalDateTime.now());
        knowledgePoint.setQuestionCount(0);
        return knowledgePointRepository.save(knowledgePoint);
    }

    @Override
    public KnowledgePoint updateKnowledgePoint(String id, KnowledgePoint knowledgePoint) {
        KnowledgePoint existing = knowledgePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        
        existing.setTitle(knowledgePoint.getTitle());
        existing.setDescription(knowledgePoint.getDescription());
        existing.setCategory(knowledgePoint.getCategory());
        existing.setUpdateTime(LocalDateTime.now());
        
        return knowledgePointRepository.save(existing);
    }

    @Override
    public void deleteKnowledgePoint(String id) {
        if (!knowledgePointRepository.existsById(id)) {
            throw new RuntimeException("知识点不存在");
        }
        knowledgePointRepository.deleteById(id);
    }

    @Override
    public KnowledgePoint getKnowledgePointById(String id) {
        return knowledgePointRepository.findById(id)
                .orElse(null);
    }

    @Override
    public List<KnowledgePoint> getAllKnowledgePoints() {
        return knowledgePointRepository.findAll();
    }

    @Override
    public List<KnowledgePoint> findHotKnowledgePoints() {
        return knowledgePointRepository.findAllByOrderByQuestionCountDesc();
    }

    @Override
    public List<KnowledgePoint> findByCategory(String category) {
        return knowledgePointRepository.findByCategory(category);
    }

    @Override
    public List<KnowledgePoint> searchByTitle(String keyword) {
        return knowledgePointRepository.findByTitleContainingIgnoreCase(keyword);
    }
}
