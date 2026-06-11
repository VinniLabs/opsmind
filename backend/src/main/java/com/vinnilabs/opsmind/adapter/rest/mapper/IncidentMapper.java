package com.vinnilabs.opsmind.adapter.rest.mapper;

import com.vinnilabs.opsmind.adapter.rest.response.IncidentResponse;
import com.vinnilabs.opsmind.application.domain.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .errorLog(incident.getErrorLog())
                .status(incident.getStatus() != null ? incident.getStatus().name() : null)
                .aiDiagnosis(incident.getAiDiagnosis())
                .aiSeverity(incident.getAiSeverity() != null ? incident.getAiSeverity().name() : null)
                .aiRootCause(incident.getAiRootCause())
                .aiImpact(incident.getAiImpact())
                .aiProposedSolution(incident.getAiProposedSolution())
                .aiProvider(incident.getAiProvider() != null ? incident.getAiProvider().name() : null)
                .realRootCause(incident.getRealRootCause())
                .appliedSolution(incident.getAppliedSolution())
                .diagnosisCorrect(incident.getDiagnosisCorrect() != null ? incident.getDiagnosisCorrect().name() : null)
                .validationNotes(incident.getValidationNotes())
                .validatedAt(incident.getValidatedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}

