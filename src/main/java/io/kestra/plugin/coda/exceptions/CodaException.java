package io.kestra.plugin.coda.exceptions;

/**
 * Base exception for all Coda API related errors.
 */
public class CodaException extends Exception {
    private final Integer statusCode;

    public CodaException(String message) {
        super(message);
        this.statusCode = null;
    }

    public CodaException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public CodaException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CodaException(String message, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        if (statusCode != null) {
            return String.format("Coda API Error (HTTP %d): %s", statusCode, super.getMessage());
        }
        return super.getMessage();
    }
}
