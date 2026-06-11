package com.vinnilabs.opsmind.application.repository;

import com.vinnilabs.opsmind.application.domain.Incident;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository {

    Incident save(Incident incident);

    Optional<Incident> findById(String id);

    List<Incident> findAll();
}

