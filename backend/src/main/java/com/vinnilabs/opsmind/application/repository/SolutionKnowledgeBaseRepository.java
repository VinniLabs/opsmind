package com.vinnilabs.opsmind.application.repository;

import com.vinnilabs.opsmind.application.domain.SolutionKnowledgeBase;

import java.util.List;

public interface SolutionKnowledgeBaseRepository {

    List<SolutionKnowledgeBase> findSimilar(String content);
}

