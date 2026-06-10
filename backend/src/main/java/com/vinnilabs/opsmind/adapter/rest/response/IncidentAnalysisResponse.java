package com.vinnilabs.opsmind.adapter.rest.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IncidentAnalysisResponse {

    String id;
    String severity;
    String rootCause;
    String impact;
    String recommendation;
}

