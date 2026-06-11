package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.Incident;

public interface UpdateIncidentAnalysisUseCase {

    Incident updateAnalysis(String id, String aiDiagnosis, String realRootCause);
}

