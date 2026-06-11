package com.vinnilabs.opsmind.application.ai;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.exception.AiProviderException;
import com.vinnilabs.opsmind.application.exception.AiProvidersUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Routes incident analysis requests across the available AI providers,
 * applying automatic fallback.
 *
 * <p>Providers are tried in the order declared by
 * {@code opsmind.ai.providers.order} (e.g. {@code OLLAMA,GROQ,GEMINI}). When a
 * provider fails with a retryable error (timeout, connection refused, missing
 * model, 429, 500, 502, 503 or 504) the router logs the fallback and tries the
 * next provider. If all providers fail an
 * {@link AiProvidersUnavailableException} is raised so the incident is kept in
 * its current status and a friendly message is returned to the front-end.</p>
 *
 * <p>Adding a new provider (Ollama, OpenRouter, OpenAI, Claude, ...) only
 * requires a new {@link AiIncidentAnalyzer} bean plus an entry in
 * {@link AiProviderType}; no change is needed here.</p>
 */
@Component
public class AiProviderRouter {

    private static final Logger log = LoggerFactory.getLogger(AiProviderRouter.class);

    private final List<AiIncidentAnalyzer> orderedProviders;

    public AiProviderRouter(List<AiIncidentAnalyzer> providers,
                            @Value("${opsmind.ai.providers.order:}") String configuredOrder) {
        List<AiProviderType> order = parseOrder(configuredOrder);

        this.orderedProviders = providers.stream()
                .sorted(Comparator.comparingInt(provider -> rank(order, provider.providerType())))
                .toList();

        log.info("AI providers registered (fallback order): {}",
                orderedProviders.stream().map(AiIncidentAnalyzer::providerType).toList());
    }

    /**
     * Parses {@code opsmind.ai.providers.order} into a list of provider types,
     * ignoring blank or unknown entries. When no valid order is configured the
     * enum declaration order is used as a safe default.
     */
    private List<AiProviderType> parseOrder(String configuredOrder) {
        List<AiProviderType> order = new ArrayList<>();
        if (configuredOrder != null && !configuredOrder.isBlank()) {
            for (String token : configuredOrder.split(",")) {
                String name = token.trim().toUpperCase();
                if (name.isEmpty()) {
                    continue;
                }
                try {
                    order.add(AiProviderType.valueOf(name));
                } catch (IllegalArgumentException ex) {
                    log.warn("Ignoring unknown AI provider in opsmind.ai.providers.order: {}", token.trim());
                }
            }
        }
        if (order.isEmpty()) {
            order = Arrays.asList(AiProviderType.values());
        }
        return order;
    }

    /**
     * Position of a provider in the configured order. Providers not listed are
     * pushed to the end while preserving deterministic behaviour.
     */
    private int rank(List<AiProviderType> order, AiProviderType type) {
        int index = order.indexOf(type);
        return index >= 0 ? index : Integer.MAX_VALUE;
    }

    public IncidentAnalysis analyze(String content) {
        if (orderedProviders.isEmpty()) {
            log.error("No AI providers are configured");
            throw new AiProvidersUnavailableException(
                    new IllegalStateException("No AI providers configured"));
        }

        AiProviderException lastError = null;

        for (int i = 0; i < orderedProviders.size(); i++) {
            AiIncidentAnalyzer provider = orderedProviders.get(i);
            AiProviderType providerType = provider.providerType();

            log.info("Provider selected: {}", providerType);

            try {
                IncidentAnalysis analysis = provider.analyze(content);
                log.info("Provider responded successfully provider={}", providerType);
                return analysis;
            } catch (AiProviderException exception) {
                lastError = exception;
                log.warn("Provider failed provider={} status={} retryable={}",
                        providerType, exception.getStatus(), exception.isRetryable(), exception);

                boolean hasNext = i + 1 < orderedProviders.size();
                if (hasNext) {
                    log.warn("Provider fallback: {}", orderedProviders.get(i + 1).providerType());
                }
            } catch (Exception exception) {
                lastError = new AiProviderException(providerType, null, false, exception);
                log.warn("Provider failed unexpectedly provider={}", providerType, exception);

                boolean hasNext = i + 1 < orderedProviders.size();
                if (hasNext) {
                    log.warn("Provider fallback: {}", orderedProviders.get(i + 1).providerType());
                }
            }
        }

        log.error("All AI providers failed; incident analysis could not be generated");
        throw new AiProvidersUnavailableException(lastError);
    }
}

