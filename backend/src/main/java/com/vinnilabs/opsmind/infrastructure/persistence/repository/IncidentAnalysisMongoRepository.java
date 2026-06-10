package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.infrastructure.persistence.entity.IncidentAnalysisEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IncidentAnalysisMongoRepository extends MongoRepository<IncidentAnalysisEntity, String> {
}