package com.vinnilabs.opsmind.application.ai;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;

public interface AiIncidentAnalyzer {
    IncidentAnalysis analyze(String content);
}

