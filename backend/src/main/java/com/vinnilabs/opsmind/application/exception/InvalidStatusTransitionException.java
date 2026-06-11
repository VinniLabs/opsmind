package com.vinnilabs.opsmind.application.exception;

import com.vinnilabs.opsmind.application.domain.IncidentStatus;

public class InvalidStatusTransitionException extends ApplicationException {

    public InvalidStatusTransitionException(IncidentStatus from, IncidentStatus to) {
        super("INVALID_STATUS_TRANSITION",
                "Invalid status transition: " + from + " -> " + to);
    }
}

