package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;

public interface AnalyzeIncidentUseCase {

    IncidentAnalysis analyze(String content);
}

