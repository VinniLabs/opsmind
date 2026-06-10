package com.vinnilabs.opsmind.application.repository;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;

import java.util.List;

public interface IncidentAnalysisRepository {

    IncidentAnalysis save(String content, IncidentAnalysis analysis);

    boolean existsById(String id);

    List<IncidentAnalysis> findSimilar(String content);
}

