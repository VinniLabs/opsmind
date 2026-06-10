package com.vinnilabs.opsmind.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IncidentAnalysisRequest {

    @NotBlank(message = "content is required")
    @Size(min = 10, max = 10000, message = "content must be between 10 and 10000 characters")
    private String content;
}

