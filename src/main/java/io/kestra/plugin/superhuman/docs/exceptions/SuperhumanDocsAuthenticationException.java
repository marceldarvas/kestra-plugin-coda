package io.kestra.plugin.superhuman.docs.exceptions;

/**
 * Exception thrown when authentication with the Superhuman Docs API fails.
 * This typically occurs when the API token is invalid or missing.
 */
public class SuperhumanDocsAuthenticationException extends SuperhumanDocsException {
    public SuperhumanDocsAuthenticationException(String message) {
        super(message, 401);
    }

    public SuperhumanDocsAuthenticationException(String message, Throwable cause) {
        super(message, 401, cause);
    }
}
