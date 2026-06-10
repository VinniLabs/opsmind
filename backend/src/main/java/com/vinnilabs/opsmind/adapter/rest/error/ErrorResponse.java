package com.vinnilabs.opsmind.adapter.rest.error;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ErrorResponse {

    String code;
    String message;
    Instant timestamp;
}
