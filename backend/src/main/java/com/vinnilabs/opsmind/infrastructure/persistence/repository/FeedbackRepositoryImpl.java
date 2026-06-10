package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.application.domain.AnalysisFeedback;
import com.vinnilabs.opsmind.application.repository.FeedbackRepository;
import com.vinnilabs.opsmind.infrastructure.persistence.entity.AnalysisFeedbackEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedbackRepositoryImpl implements FeedbackRepository {

    private final AnalysisFeedbackMongoRepository mongoRepository;

    @Override
    public AnalysisFeedback save(AnalysisFeedback feedback) {
        AnalysisFeedbackEntity saved = mongoRepository.save(toEntity(feedback));
        return toDomain(saved);
    }

    private AnalysisFeedbackEntity toEntity(AnalysisFeedback feedback) {
        return AnalysisFeedbackEntity.builder()
                .analysisId(feedback.getAnalysisId())
                .useful(feedback.isUseful())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private AnalysisFeedback toDomain(AnalysisFeedbackEntity entity) {
        return AnalysisFeedback.builder()
                .id(entity.getId())
                .analysisId(entity.getAnalysisId())
                .useful(entity.isUseful())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
