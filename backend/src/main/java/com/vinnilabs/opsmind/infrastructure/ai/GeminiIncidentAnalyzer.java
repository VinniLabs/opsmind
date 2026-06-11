package com.vinnilabs.opsmind.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinnilabs.opsmind.application.ai.AiIncidentAnalyzer;
import com.vinnilabs.opsmind.application.ai.AiProviderType;
import com.vinnilabs.opsmind.application.ai.IncidentAnalysisPromptBuilder;
import com.vinnilabs.opsmind.application.ai.IncidentAnalysisResponseParser;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiIncidentAnalyzer implements AiIncidentAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GeminiIncidentAnalyzer.class);

    private final RestClient.Builder restClientBuilder;
    private final IncidentAnalysisPromptBuilder promptBuilder;
    private final IncidentAnalysisResponseParser responseParser;

    @Value("${opsmind.ai.gemini.api-key}")
    private String apiKey;

    @Value("${opsmind.ai.gemini.model}")
    private String model;

    @Value("${opsmind.ai.gemini.url}")
    private String baseUrl;

    @Override
    public AiProviderType providerType() {
        return AiProviderType.GEMINI;
    }

    @Override
    public IncidentAnalysis analyze(String content) {
        log.info("Starting AI incident analysis provider=GEMINI model={}", model);

        String text;
        try {
            String prompt = promptBuilder.build(content);

            GeminiRequest request = new GeminiRequest(
                    List.of(new Content(List.of(new Part(prompt))))
            );

            GeminiResponse response = restClientBuilder.clone()
                    .requestFactory(requestFactory())
                    .build()
                    .post()
                    .uri("%s/%s:generateContent?key=%s".formatted(baseUrl, model, apiKey))
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            text = response.candidates()
                    .getFirst()
                    .content()
                    .parts()
                    .getFirst()
                    .text();

            log.debug("AI response received provider=GEMINI length={}", text.length());
        } catch (Exception exception) {
            log.error("AI incident analysis call failed provider=GEMINI model={}", model, exception);
            throw AiProviderException.from(AiProviderType.GEMINI, exception);
        }

        try {
            IncidentAnalysis analysis = responseParser.parse(text, AiProviderType.GEMINI);
            log.info("AI incident analysis completed provider=GEMINI severity={}", analysis.getSeverity());
            return analysis;
        } catch (Exception exception) {
            log.error("Failed to parse AI response provider=GEMINI", exception);
            throw new AiProviderException(AiProviderType.GEMINI, null, false, exception);
        }
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }

    private record GeminiRequest(List<Content> contents) {
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Candidate(ContentResponse content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentResponse(List<PartResponse> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PartResponse(String text) {
    }
}
