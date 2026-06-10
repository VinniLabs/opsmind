package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.infrastructure.persistence.entity.SolutionKnowledgeBaseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SolutionKnowledgeBaseMongoRepository extends MongoRepository<SolutionKnowledgeBaseEntity, String> {

    @Query("{ 'validated': true, $or: [ " +
           "{ 'symptoms':          { $regex: ?0, $options: 'i' } }, " +
           "{ 'tags':              { $regex: ?0, $options: 'i' } }, " +
           "{ 'title':             { $regex: ?0, $options: 'i' } }, " +
           "{ 'category':          { $regex: ?0, $options: 'i' } }, " +
           "{ 'confirmedRootCause':{ $regex: ?0, $options: 'i' } } " +
           "] }")
    List<SolutionKnowledgeBaseEntity> findValidatedByKeyword(String keyword);
}

