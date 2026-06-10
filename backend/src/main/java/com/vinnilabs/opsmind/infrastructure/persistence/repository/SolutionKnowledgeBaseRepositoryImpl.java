package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.application.domain.SolutionKnowledgeBase;
import com.vinnilabs.opsmind.application.repository.SolutionKnowledgeBaseRepository;
import com.vinnilabs.opsmind.infrastructure.persistence.entity.SolutionKnowledgeBaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class SolutionKnowledgeBaseRepositoryImpl implements SolutionKnowledgeBaseRepository {

    private static final int MAX_RESULTS = 5;
    private static final int MIN_KEYWORD_LENGTH = 4;

    private final SolutionKnowledgeBaseMongoRepository mongoRepository;

    @Override
    public List<SolutionKnowledgeBase> findSimilar(String content) {
        List<String> keywords = extractKeywords(content);

        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        // Accumulate unique results by id, maintaining insertion order
        Map<String, SolutionKnowledgeBaseEntity> uniqueResults = new LinkedHashMap<>();

        for (String keyword : keywords) {
            mongoRepository.findValidatedByKeyword(keyword)
                    .forEach(entity -> uniqueResults.putIfAbsent(entity.getId(), entity));

            if (uniqueResults.size() >= MAX_RESULTS) {
                break;
            }
        }

        return uniqueResults.values().stream()
                .limit(MAX_RESULTS)
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

    private SolutionKnowledgeBase toDomain(SolutionKnowledgeBaseEntity entity) {
        return SolutionKnowledgeBase.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .symptoms(entity.getSymptoms())
                .confirmedRootCause(entity.getConfirmedRootCause())
                .solution(entity.getSolution())
                .tags(entity.getTags())
                .validated(entity.isValidated())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

