package io.kestra.plugin.coda.exceptions;

/**
 * Exception thrown when a requested Coda resource (document, table, row, etc.) is not found.
 */
public class CodaResourceNotFoundException extends CodaException {
    public CodaResourceNotFoundException(String message) {
        super(message, 404);
    }

    public CodaResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with ID '%s' not found", resourceType, resourceId), 404);
    }

    public CodaResourceNotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}
