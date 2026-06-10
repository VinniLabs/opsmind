package com.vinnilabs.opsmind.adapter.rest.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class FeedbackResponse {

    String id;
    String analysisId;
    boolean useful;
    String comment;
    LocalDateTime createdAt;
}
