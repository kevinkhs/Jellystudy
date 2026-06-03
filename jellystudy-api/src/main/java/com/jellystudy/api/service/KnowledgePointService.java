package com.jellystudy.api.service;

import com.jellystudy.api.entity.KnowledgePoint;
import java.util.List;

public interface KnowledgePointService {

    KnowledgePoint createKnowledgePoint(KnowledgePoint knowledgePoint);

    KnowledgePoint updateKnowledgePoint(String id, KnowledgePoint knowledgePoint);

    void deleteKnowledgePoint(String id);

    KnowledgePoint getKnowledgePointById(String id);

    List<KnowledgePoint> getAllKnowledgePoints();

    List<KnowledgePoint> findHotKnowledgePoints();

    List<KnowledgePoint> findByCategory(String category);

    List<KnowledgePoint> searchByTitle(String keyword);
}
