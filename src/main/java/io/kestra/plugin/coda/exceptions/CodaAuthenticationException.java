package io.kestra.plugin.coda.exceptions;

/**
 * Exception thrown when authentication with the Coda API fails.
 * This typically occurs when the API token is invalid or missing.
 */
public class CodaAuthenticationException extends CodaException {
    public CodaAuthenticationException(String message) {
        super(message, 401);
    }

    public CodaAuthenticationException(String message, Throwable cause) {
        super(message, 401, cause);
    }
}
