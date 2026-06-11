package com.vinnilabs.opsmind.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateIncidentStatusRequest {

    @NotBlank(message = "status is required")
    private String status;
}

