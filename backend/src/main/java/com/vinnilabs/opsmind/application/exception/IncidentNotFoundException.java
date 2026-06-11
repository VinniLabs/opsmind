package com.vinnilabs.opsmind.application.exception;

public class IncidentNotFoundException extends ApplicationException {

    public IncidentNotFoundException(String incidentId) {
        super("INCIDENT_NOT_FOUND", "Incident not found: " + incidentId);
    }
}

