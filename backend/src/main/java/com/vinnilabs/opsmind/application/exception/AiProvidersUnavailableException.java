package com.vinnilabs.opsmind.application.exception;

/**
 * Raised when every configured AI provider failed to produce an analysis.
 * Maps to a user-friendly message on the API layer.
 */
public class AiProvidersUnavailableException extends ApplicationException {

    public static final String FRIENDLY_MESSAGE =
            "Provedor de IA temporariamente indisponível. Tente novamente em alguns instantes.";

    public AiProvidersUnavailableException(Throwable cause) {
        super("AI_PROVIDERS_UNAVAILABLE", FRIENDLY_MESSAGE, cause);
    }
}

