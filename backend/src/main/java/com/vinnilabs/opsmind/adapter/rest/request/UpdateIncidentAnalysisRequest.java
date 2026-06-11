package com.vinnilabs.opsmind.adapter.rest.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateIncidentAnalysisRequest {

    @Size(max = 10000, message = "aiDiagnosis must be at most 10000 characters")
    private String aiDiagnosis;

    @Size(max = 10000, message = "realRootCause must be at most 10000 characters")
    private String realRootCause;
}

