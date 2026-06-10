package com.vinnilabs.opsmind.infrastructure.persistence.entity;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@Document(collection = "solution_knowledge_base")
public class SolutionKnowledgeBaseEntity {

    @Id
    String id;

    String title;

    String category;

    String symptoms;

    String confirmedRootCause;

    String solution;

    List<String> tags;

    boolean validated;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}

