package io.kestra.plugin.superhuman.docs.exceptions;

/**
 * Base exception for all Superhuman Docs API related errors.
 */
public class SuperhumanDocsException extends Exception {
    private final Integer statusCode;

    public SuperhumanDocsException(String message) {
        super(message);
        this.statusCode = null;
    }

    public SuperhumanDocsException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public SuperhumanDocsException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public SuperhumanDocsException(String message, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        if (statusCode != null) {
            return String.format("Superhuman Docs API Error (HTTP %d): %s", statusCode, super.getMessage());
        }
        return super.getMessage();
    }
}
