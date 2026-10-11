package io.kestra.plugin.superhuman.docs.exceptions;

/**
 * Exception thrown when a requested Superhuman Docs resource (document, table, row, etc.) is not found.
 */
public class SuperhumanDocsResourceNotFoundException extends SuperhumanDocsException {
    public SuperhumanDocsResourceNotFoundException(String message) {
        super(message, 404);
    }

    public SuperhumanDocsResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with ID '%s' not found", resourceType, resourceId), 404);
    }

    public SuperhumanDocsResourceNotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}
