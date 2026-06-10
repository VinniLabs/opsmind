package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.infrastructure.persistence.entity.AnalysisFeedbackEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalysisFeedbackMongoRepository extends MongoRepository<AnalysisFeedbackEntity, String> {
}
