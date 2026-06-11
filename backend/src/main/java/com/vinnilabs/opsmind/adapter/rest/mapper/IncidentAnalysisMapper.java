package com.vinnilabs.opsmind.adapter.rest.mapper;

import com.vinnilabs.opsmind.adapter.rest.response.IncidentAnalysisResponse;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import org.springframework.stereotype.Component;

@Component
public class IncidentAnalysisMapper {

    public IncidentAnalysisResponse toResponse(IncidentAnalysis analysis) {
        return IncidentAnalysisResponse.builder()
                .id(analysis.getId())
                .severity(analysis.getSeverity().name())
                .rootCause(analysis.getRootCause())
                .impact(analysis.getImpact())
                .recommendation(analysis.getRecommendation())
                .provider(analysis.getProvider() != null ? analysis.getProvider().name() : null)
                .build();
    }
}
