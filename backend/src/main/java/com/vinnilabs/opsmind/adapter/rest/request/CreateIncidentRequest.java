package com.vinnilabs.opsmind.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateIncidentRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    @NotBlank(message = "description is required")
    @Size(max = 5000, message = "description must be at most 5000 characters")
    private String description;

    @Size(max = 20000, message = "errorLog must be at most 20000 characters")
    private String errorLog;
}

