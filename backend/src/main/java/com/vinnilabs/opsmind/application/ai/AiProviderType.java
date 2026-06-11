package com.vinnilabs.opsmind.application.ai;

/**
 * Supported AI providers.
 *
 * <p>The declaration order defines the default fallback order used by
 * {@link AiProviderRouter}: the router tries providers from top to bottom.
 * Future providers (e.g. OLLAMA, OPENROUTER, OPENAI, CLAUDE) can be added
 * here and implemented as new {@link AiIncidentAnalyzer} beans without
 * changing the router logic.</p>
 */
public enum AiProviderType {
    OLLAMA,
    GROQ,
    GEMINI
}

