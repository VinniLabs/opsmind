package com.vinnilabs.opsmind.application.exception;

import com.vinnilabs.opsmind.application.ai.AiProviderType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Set;

/**
 * Raised by an individual AI provider when a call fails. Carries enough context
 * for the {@link com.vinnilabs.opsmind.application.ai.AiProviderRouter} to decide
 * whether a fallback to the next provider should be attempted.
 */
public class AiProviderException extends RuntimeException {

    /**
     * HTTP statuses (plus timeouts) that should trigger a fallback to the next provider.
     */
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(429, 500, 502, 503, 504);

    private final AiProviderType provider;
    private final Integer status;
    private final boolean retryable;

    public AiProviderException(AiProviderType provider, Integer status, boolean retryable, Throwable cause) {
        super("AI provider " + provider + " call failed"
                + (status != null ? " (status " + status + ")" : ""), cause);
        this.provider = provider;
        this.status = status;
        this.retryable = retryable;
    }

    /**
     * Classifies a raw exception thrown while calling a provider into an
     * {@link AiProviderException}, detecting retryable HTTP statuses and timeouts.
     */
    public static AiProviderException from(AiProviderType provider, Throwable cause) {
        if (cause instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            return new AiProviderException(provider, statusCode, RETRYABLE_STATUSES.contains(statusCode), cause);
        }
        if (cause instanceof ResourceAccessException) {
            // Connection failure / read timeout.
            return new AiProviderException(provider, null, true, cause);
        }
        return new AiProviderException(provider, null, false, cause);
    }

    public AiProviderType getProvider() {
        return provider;
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

