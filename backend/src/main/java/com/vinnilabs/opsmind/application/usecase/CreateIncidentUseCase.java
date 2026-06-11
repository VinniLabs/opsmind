package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.Incident;

public interface CreateIncidentUseCase {

    Incident create(String title, String description, String errorLog);
}

