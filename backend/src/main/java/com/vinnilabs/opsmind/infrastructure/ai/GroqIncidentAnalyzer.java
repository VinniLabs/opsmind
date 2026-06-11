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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * Groq provider (OpenAI-compatible Chat Completions API).
 *
 * <p>Enabled only when {@code opsmind.ai.groq.enabled=true}. Acts as the
 * automatic fallback after Gemini.</p>
 */
@Component
@ConditionalOnProperty(name = "opsmind.ai.groq.enabled", havingValue = "true")
@RequiredArgsConstructor
public class GroqIncidentAnalyzer implements AiIncidentAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GroqIncidentAnalyzer.class);

    private final RestClient.Builder restClientBuilder;
    private final IncidentAnalysisPromptBuilder promptBuilder;
    private final IncidentAnalysisResponseParser responseParser;

    @Value("${opsmind.ai.groq.api-key}")
    private String apiKey;

    @Value("${opsmind.ai.groq.base-url}")
    private String baseUrl;

    @Value("${opsmind.ai.groq.model}")
    private String model;

    @Override
    public AiProviderType providerType() {
        return AiProviderType.GROQ;
    }

    @Override
    public IncidentAnalysis analyze(String content) {
        log.info("Starting AI incident analysis provider=GROQ model={}", model);

        String text;
        try {
            String prompt = promptBuilder.build(content);

            GroqRequest request = new GroqRequest(
                    model,
                    List.of(new Message("user", prompt))
            );

            GroqResponse response = restClientBuilder.clone()
                    .requestFactory(requestFactory())
                    .build()
                    .post()
                    .uri(baseUrl + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            text = response.choices()
                    .getFirst()
                    .message()
                    .content();

            log.debug("AI response received provider=GROQ length={}", text.length());
        } catch (Exception exception) {
            log.error("AI incident analysis call failed provider=GROQ model={}", model, exception);
            throw AiProviderException.from(AiProviderType.GROQ, exception);
        }

        try {
            IncidentAnalysis analysis = responseParser.parse(text, AiProviderType.GROQ);
            log.info("AI incident analysis completed provider=GROQ severity={}", analysis.getSeverity());
            return analysis;
        } catch (Exception exception) {
            log.error("Failed to parse AI response provider=GROQ", exception);
            throw new AiProviderException(AiProviderType.GROQ, null, false, exception);
        }
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }

    private record GroqRequest(String model, List<Message> messages) {
    }

    private record Message(String role, String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GroqResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {
    }
}

