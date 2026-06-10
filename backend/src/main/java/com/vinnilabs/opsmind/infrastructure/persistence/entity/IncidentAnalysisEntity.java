package com.vinnilabs.opsmind.infrastructure.persistence.entity;

import com.vinnilabs.opsmind.application.domain.IncidentSeverity;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Value
@Builder
@Document(collection = "incident_analysis_history")
public class IncidentAnalysisEntity {

    @Id
    String id;

    String content;

    IncidentSeverity severity;

    String rootCause;

    String impact;

    String recommendation;

    LocalDateTime createdAt;
}