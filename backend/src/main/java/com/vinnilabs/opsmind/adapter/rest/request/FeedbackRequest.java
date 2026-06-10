package com.vinnilabs.opsmind.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedbackRequest {

    @NotBlank(message = "analysisId is required")
    private String analysisId;

    @NotNull(message = "useful is required")
    private Boolean useful;

    @Size(max = 1000, message = "comment must be at most 1000 characters")
    private String comment;
}
