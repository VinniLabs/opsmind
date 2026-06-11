package com.vinnilabs.opsmind.application.domain;

import com.vinnilabs.opsmind.application.ai.AiProviderType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class Incident {

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
