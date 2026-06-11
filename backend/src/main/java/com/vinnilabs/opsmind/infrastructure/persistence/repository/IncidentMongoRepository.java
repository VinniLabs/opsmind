package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IncidentMongoRepository extends MongoRepository<IncidentEntity, String> {
}

