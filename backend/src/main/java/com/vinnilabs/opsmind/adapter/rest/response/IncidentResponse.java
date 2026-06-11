package com.vinnilabs.opsmind.adapter.rest.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class IncidentResponse {

    String id;
    String title;
    String description;
    String errorLog;
    String status;
    String aiDiagnosis;
    String aiSeverity;
    String aiRootCause;
    String aiImpact;
    String aiProposedSolution;
    String aiProvider;
    String realRootCause;
    String appliedSolution;
    String diagnosisCorrect;
    String validationNotes;
    LocalDateTime validatedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
