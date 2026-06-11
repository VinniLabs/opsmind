package com.vinnilabs.opsmind.infrastructure.persistence.entity;

import com.vinnilabs.opsmind.application.ai.AiProviderType;
import com.vinnilabs.opsmind.application.domain.DiagnosisAccuracy;
import com.vinnilabs.opsmind.application.domain.IncidentSeverity;
import com.vinnilabs.opsmind.application.domain.IncidentStatus;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Value
@Builder
@Document(collection = "incidents")
public class IncidentEntity {

    @Id
    String id;

    String title;

    String description;

    String errorLog;

    IncidentStatus status;

    String aiDiagnosis;

    IncidentSeverity aiSeverity;

    String aiRootCause;

    String aiImpact;

    String aiProposedSolution;

    AiProviderType aiProvider;

    String realRootCause;

    String appliedSolution;

    DiagnosisAccuracy diagnosisCorrect;

    String validationNotes;

    LocalDateTime validatedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
