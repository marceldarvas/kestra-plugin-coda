package io.kestra.plugin.superhuman.docs.exceptions;

/**
 * Exception thrown when the Superhuman Docs API rate limit is exceeded.
 * Clients should implement retry logic with exponential backoff when this exception occurs.
 */
public class SuperhumanDocsRateLimitException extends SuperhumanDocsException {
    private final Integer retryAfterSeconds;

    public SuperhumanDocsRateLimitException(String message) {
        super(message, 429);
        this.retryAfterSeconds = null;
    }

    public SuperhumanDocsRateLimitException(String message, Integer retryAfterSeconds) {
        super(message, 429);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public SuperhumanDocsRateLimitException(String message, Throwable cause) {
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
