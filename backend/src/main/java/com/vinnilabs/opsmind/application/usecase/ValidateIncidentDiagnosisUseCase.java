package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.DiagnosisAccuracy;
import com.vinnilabs.opsmind.application.domain.Incident;

/**
 * Records the human validation of the AI diagnosis on an incident.
 */
public interface ValidateIncidentDiagnosisUseCase {

    Incident validateDiagnosis(String id,
                               String realRootCause,
                               String appliedSolution,
                               DiagnosisAccuracy diagnosisCorrect,
                               String validationNotes);
}

