package com.vinnilabs.opsmind.application.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.domain.IncidentSeverity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Parses the raw JSON text returned by any AI provider into an
 * {@link IncidentAnalysis}. Shared by every provider to avoid duplication.
 */
@Component
@RequiredArgsConstructor
public class IncidentAnalysisResponseParser {

    private static final Logger log = LoggerFactory.getLogger(IncidentAnalysisResponseParser.class);

    private final ObjectMapper objectMapper;

    public IncidentAnalysis parse(String rawText, AiProviderType provider) throws Exception {
        AiAnalysisResponse analysis = objectMapper.readValue(cleanJson(rawText), AiAnalysisResponse.class);

        return IncidentAnalysis.builder()
                .severity(resolveSeverity(analysis.severity()))
                .rootCause(analysis.rootCause())
                .impact(analysis.impact())
                .recommendation(analysis.recommendation())
                .provider(provider)
                .build();
    }

    private String cleanJson(String text) {
        return text
                .replace("```json", "")
                .replace("```", "")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    private IncidentSeverity resolveSeverity(String severity) {
        try {
            return IncidentSeverity.valueOf(severity.toUpperCase());
        } catch (Exception exception) {
            log.warn("Unrecognized severity value={}, defaulting to MEDIUM", severity);
            return IncidentSeverity.MEDIUM;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiAnalysisResponse(
            String severity,
            String rootCause,
            String impact,
            String recommendation
    ) {
    }
}

