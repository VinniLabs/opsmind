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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * Ollama provider (local AI via the native {@code /api/generate} endpoint).
 *
 * <p>Enabled only when {@code opsmind.ai.ollama.enabled=true}. The model is
 * defined exclusively by configuration ({@code opsmind.ai.ollama.model}); the
 * code never assumes which model is in use. The shared
 * {@link IncidentAnalysisPromptBuilder} and {@link IncidentAnalysisResponseParser}
 * are reused so the exact same scope rules and output contract
 * (severity/rootCause/impact/recommendation) apply to every provider.</p>
 */
@Component
@ConditionalOnProperty(name = "opsmind.ai.ollama.enabled", havingValue = "true")
@RequiredArgsConstructor
public class OllamaIncidentAnalyzer implements AiIncidentAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(OllamaIncidentAnalyzer.class);

    private final RestClient.Builder restClientBuilder;
    private final IncidentAnalysisPromptBuilder promptBuilder;
    private final IncidentAnalysisResponseParser responseParser;

    @Value("${opsmind.ai.ollama.base-url}")
    private String baseUrl;

    @Value("${opsmind.ai.ollama.model}")
    private String model;

    @Override
    public AiProviderType providerType() {
        return AiProviderType.OLLAMA;
    }

    @Override
    public IncidentAnalysis analyze(String content) {
        log.info("Starting AI incident analysis provider=OLLAMA model={}", model);

        String text;
        try {
            String prompt = promptBuilder.build(content);

            OllamaRequest request = new OllamaRequest(model, prompt, false);

            OllamaResponse response = restClientBuilder.clone()
                    .requestFactory(requestFactory())
                    .build()
                    .post()
                    .uri(baseUrl + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response == null || response.response() == null) {
                throw new IllegalStateException("Ollama returned an empty response body");
            }

            text = response.response();

            log.debug("AI response received provider=OLLAMA length={}", text.length());
        } catch (Exception exception) {
            log.error("AI incident analysis call failed provider=OLLAMA model={}", model, exception);
            throw AiProviderException.from(AiProviderType.OLLAMA, exception);
        }

        try {
            IncidentAnalysis analysis = responseParser.parse(text, AiProviderType.OLLAMA);
            log.info("AI incident analysis completed provider=OLLAMA severity={}", analysis.getSeverity());
            return analysis;
        } catch (Exception exception) {
            log.error("Failed to parse AI response provider=OLLAMA", exception);
            throw new AiProviderException(AiProviderType.OLLAMA, null, false, exception);
        }
    }

    /**
     * Validates, at startup, that the configured model is available in Ollama by
     * querying {@code GET /api/tags}. The application is never brought down: a
     * clear log is emitted whether the model is present or not.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguredModel() {
        try {
            OllamaTagsResponse tags = restClientBuilder.clone()
                    .requestFactory(requestFactory())
                    .build()
                    .get()
                    .uri(baseUrl + "/api/tags")
                    .retrieve()
                    .body(OllamaTagsResponse.class);

            List<String> availableModels = tags == null || tags.models() == null
                    ? List.of()
                    : tags.models().stream().map(OllamaModel::name).toList();

            if (availableModels.contains(model)) {
                log.info("Loaded Ollama model: {}", model);
            } else {
                log.error("Configured model not found: {}{}Available models:{}{}",
                        model,
                        System.lineSeparator(),
                        System.lineSeparator(),
                        formatAvailable(availableModels));
            }
        } catch (Exception exception) {
            log.error("Could not validate Ollama model '{}' at {} (Ollama may be unavailable): {}",
                    model, baseUrl, exception.getMessage());
        }
    }

    private String formatAvailable(List<String> availableModels) {
        if (availableModels.isEmpty()) {
            return "* (none)";
        }
        return availableModels.stream()
                .map(name -> "* " + name)
                .reduce((a, b) -> a + System.lineSeparator() + b)
                .orElse("* (none)");
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(120));
        return factory;
    }

    private record OllamaRequest(String model, String prompt, boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaResponse(String response) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaTagsResponse(List<OllamaModel> models) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaModel(String name) {
    }
}

