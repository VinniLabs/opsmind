package com.vinnilabs.opsmind.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValidateIncidentRequest {

    @Size(max = 10000, message = "realRootCause must be at most 10000 characters")
    private String realRootCause;

    @Size(max = 10000, message = "appliedSolution must be at most 10000 characters")
    private String appliedSolution;

    @NotBlank(message = "diagnosisCorrect is required")
    private String diagnosisCorrect;

    @Size(max = 10000, message = "validationNotes must be at most 10000 characters")
    private String validationNotes;
}

