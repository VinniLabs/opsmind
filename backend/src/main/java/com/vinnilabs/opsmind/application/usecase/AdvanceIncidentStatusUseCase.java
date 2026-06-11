package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.Incident;
import com.vinnilabs.opsmind.application.domain.IncidentStatus;

public interface AdvanceIncidentStatusUseCase {

    Incident advanceStatus(String id, IncidentStatus newStatus);
}

