package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.Incident;

/**
 * Re-runs the AI analysis for an existing incident without changing its status.
 * Updates {@code aiDiagnosis} only when the analysis succeeds.
 */
public interface ReanalyzeIncidentUseCase {

    Incident reanalyze(String id);
}

