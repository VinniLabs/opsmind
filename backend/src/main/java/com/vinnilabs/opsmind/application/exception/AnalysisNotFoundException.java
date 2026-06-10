package com.vinnilabs.opsmind.application.exception;

public class AnalysisNotFoundException extends ApplicationException {

    public AnalysisNotFoundException(String analysisId) {
        super("ANALYSIS_NOT_FOUND", "Analysis not found: " + analysisId);
    }
}
