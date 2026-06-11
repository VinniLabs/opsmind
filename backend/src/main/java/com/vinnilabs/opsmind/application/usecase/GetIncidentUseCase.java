package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.Incident;

public interface GetIncidentUseCase {

    Incident getById(String id);
}

