package com.vinnilabs.opsmind.application.domain;

import com.vinnilabs.opsmind.application.ai.AiProviderType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class IncidentAnalysis {

    String id;
    IncidentSeverity severity;
    String rootCause;
    String impact;
    String recommendation;
    AiProviderType provider;
}
