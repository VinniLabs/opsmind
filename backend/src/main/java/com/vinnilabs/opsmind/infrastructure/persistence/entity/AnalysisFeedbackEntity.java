package com.vinnilabs.opsmind.infrastructure.persistence.entity;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Value
@Builder
@Document(collection = "analysis_feedback")
public class AnalysisFeedbackEntity {

    @Id
    String id;

    String analysisId;

    boolean useful;

    String comment;

    LocalDateTime createdAt;
}
