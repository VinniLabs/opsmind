package com.vinnilabs.opsmind.application.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AnalysisFeedback {

    String id;
    String analysisId;
    boolean useful;
    String comment;
    LocalDateTime createdAt;
}
