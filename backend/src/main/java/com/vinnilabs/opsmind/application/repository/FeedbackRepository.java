package com.vinnilabs.opsmind.application.repository;

import com.vinnilabs.opsmind.application.domain.AnalysisFeedback;

public interface FeedbackRepository {

    AnalysisFeedback save(AnalysisFeedback feedback);
}
