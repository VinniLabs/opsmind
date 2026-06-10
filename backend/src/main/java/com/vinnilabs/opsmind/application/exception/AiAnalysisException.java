package com.vinnilabs.opsmind.application.exception;

public class AiAnalysisException extends ApplicationException {

    public AiAnalysisException(Throwable cause) {
        super("AI_ANALYSIS_FAILED", "Failed to analyze incident with AI provider", cause);
    }
}
