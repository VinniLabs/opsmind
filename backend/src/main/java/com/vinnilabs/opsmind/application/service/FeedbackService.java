package com.vinnilabs.opsmind.application.service;

import com.vinnilabs.opsmind.application.domain.AnalysisFeedback;
import com.vinnilabs.opsmind.application.exception.AnalysisNotFoundException;
import com.vinnilabs.opsmind.application.repository.FeedbackRepository;
import com.vinnilabs.opsmind.application.repository.IncidentAnalysisRepository;
import com.vinnilabs.opsmind.application.usecase.SubmitFeedbackUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackService implements SubmitFeedbackUseCase {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final IncidentAnalysisRepository incidentAnalysisRepository;

    @Override
    public AnalysisFeedback submit(String analysisId, boolean useful, String comment) {
        log.info("Submitting feedback analysisId={} useful={}", analysisId, useful);

        if (!incidentAnalysisRepository.existsById(analysisId)) {
            log.warn("Feedback rejected analysis not found analysisId={}", analysisId);
            throw new AnalysisNotFoundException(analysisId);
        }

        AnalysisFeedback feedback = AnalysisFeedback.builder()
                .analysisId(analysisId)
                .useful(useful)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        AnalysisFeedback saved = feedbackRepository.save(feedback);

        log.info("Feedback saved feedbackId={} analysisId={}", saved.getId(), saved.getAnalysisId());

        return saved;
    }
}
