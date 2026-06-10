package com.vinnilabs.opsmind.adapter.rest.mapper;

import com.vinnilabs.opsmind.adapter.rest.response.FeedbackResponse;
import com.vinnilabs.opsmind.application.domain.AnalysisFeedback;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackResponse toResponse(AnalysisFeedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .analysisId(feedback.getAnalysisId())
                .useful(feedback.isUseful())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
