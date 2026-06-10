package com.vinnilabs.opsmind.application.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IncidentAnalysis {

    String id;
    IncidentSeverity severity;
    String rootCause;
    String impact;
    String recommendation;
}

