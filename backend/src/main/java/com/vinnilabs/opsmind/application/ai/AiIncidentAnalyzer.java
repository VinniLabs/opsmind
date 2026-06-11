package com.vinnilabs.opsmind.application.ai;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;

/**
 * Reusable contract implemented by every AI provider (Gemini, Groq and future
 * integrations such as Ollama, OpenRouter, OpenAI or Claude).
 */
public interface AiIncidentAnalyzer {

    IncidentAnalysis analyze(String content);

    /**
     * Identifies which provider backs this implementation. Used by the
     * {@link AiProviderRouter} to order providers and to produce traceable logs.
     */
    AiProviderType providerType();
}

