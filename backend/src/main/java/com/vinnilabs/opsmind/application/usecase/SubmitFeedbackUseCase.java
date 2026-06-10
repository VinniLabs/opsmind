package com.vinnilabs.opsmind.application.usecase;

import com.vinnilabs.opsmind.application.domain.AnalysisFeedback;

public interface SubmitFeedbackUseCase {

    AnalysisFeedback submit(String analysisId, boolean useful, String comment);
}
