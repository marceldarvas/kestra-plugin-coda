package io.kestra.plugin.coda.exceptions;

/**
 * Exception thrown when the Coda API rate limit is exceeded.
 * Clients should implement retry logic with exponential backoff when this exception occurs.
 */
public class CodaRateLimitException extends CodaException {
    private final Integer retryAfterSeconds;

    public CodaRateLimitException(String message) {
        super(message, 429);
        this.retryAfterSeconds = null;
    }

    public CodaRateLimitException(String message, Integer retryAfterSeconds) {
        super(message, 429);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public CodaRateLimitException(String message, Throwable cause) {
        super(message, 429, cause);
        this.retryAfterSeconds = null;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    @Override
    public String getMessage() {
        if (retryAfterSeconds != null) {
            return super.getMessage() + String.format(". Retry after %d seconds.", retryAfterSeconds);
        }
        return super.getMessage();
    }
}
