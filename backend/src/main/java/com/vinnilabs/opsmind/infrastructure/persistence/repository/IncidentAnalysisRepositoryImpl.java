package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.repository.IncidentAnalysisRepository;
import com.vinnilabs.opsmind.infrastructure.persistence.entity.IncidentAnalysisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class IncidentAnalysisRepositoryImpl implements IncidentAnalysisRepository {

    private static final int MAX_SIMILAR = 3;
    private static final int MIN_KEYWORD_LENGTH = 4;

    private final IncidentAnalysisMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public IncidentAnalysis save(String content, IncidentAnalysis analysis) {
        IncidentAnalysisEntity saved = mongoRepository.save(IncidentAnalysisEntity.builder()
                .content(content)
                .severity(analysis.getSeverity())
                .rootCause(analysis.getRootCause())
                .impact(analysis.getImpact())
                .recommendation(analysis.getRecommendation())
                .createdAt(LocalDateTime.now())
                .build());

        return toDomain(saved);
    }

    @Override
    public boolean existsById(String id) {
        return mongoRepository.existsById(id);
    }

    @Override
    public List<IncidentAnalysis> findSimilar(String content) {
        List<String> keywords = extractKeywords(content);

        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Criteria> keywordCriteria = keywords.stream()
                .map(kw -> Criteria.where("content").regex(kw, "i"))
                .toList();

        Query query = new Query(new Criteria().orOperator(keywordCriteria))
                .limit(MAX_SIMILAR);

        return mongoTemplate.find(query, IncidentAnalysisEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private List<String> extractKeywords(String content) {
        return Arrays.stream(content.split("[\\s\\W]+"))
                .map(String::toLowerCase)
                .filter(word -> word.length() >= MIN_KEYWORD_LENGTH)
                .distinct()
                .toList();
    }

    private IncidentAnalysis toDomain(IncidentAnalysisEntity entity) {
        return IncidentAnalysis.builder()
                .id(entity.getId())
                .severity(entity.getSeverity())
                .rootCause(entity.getRootCause())
                .impact(entity.getImpact())
                .recommendation(entity.getRecommendation())
                .provider(entity.getProvider())
                .build();
    }
}

