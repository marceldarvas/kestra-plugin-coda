package io.kestra.plugin.coda.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.http.client.HttpClientException;
import io.kestra.core.http.client.HttpClientResponseException;
import io.kestra.core.http.client.configurations.HttpConfiguration;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.exceptions.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;

/**
 * Manages HTTP connections and communication with the Coda API.
 */
public class CodaConnection {
    private static final String BASE_URL = "https://coda.io/apis/v1";
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule());

    private final RunContext runContext;
    private final HttpConfiguration options;
    private final String apiToken;
    private final Logger logger;

    /**
     * Creates a new Coda API connection.
     *
     * @param runContext The Kestra run context
     * @param apiToken The Coda API token for authentication
     * @param logger Logger instance for logging
     * @param options HTTP client configuration options
     */
    public CodaConnection(RunContext runContext, String apiToken, Logger logger, HttpConfiguration options) {
        this.runContext = runContext;
        this.apiToken = apiToken;
        this.logger = logger;
        this.options = options;
    }

    /**
     * Executes a GET request to the Coda API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws CodaException if the request fails
     */
    public <T> T get(String endpoint, Class<T> responseType) throws CodaException {
        URI uri = buildUri(endpoint);
        HttpRequest request = HttpRequest.builder()
            .uri(uri)
            .method("GET")
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("Content-Type", "application/json")
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes a GET request to the Coda API with generic type support.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param typeReference The TypeReference for generic types
     * @return The deserialized response
     * @throws CodaException if the request fails
     */
    public <T> T get(String endpoint, TypeReference<T> typeReference) throws CodaException {
        URI uri = buildUri(endpoint);
        HttpRequest request = HttpRequest.builder()
            .uri(uri)
            .method("GET")
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("Content-Type", "application/json")
            .build();

        return executeRequest(request, typeReference);
    }

    /**
     * Executes a POST request to the Coda API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param requestBody The request body object
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws CodaException if the request fails
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws CodaException {
        URI uri = buildUri(endpoint);
        
        try {
            String json = MAPPER.writeValueAsString(requestBody);
            logger.debug("POST {} with body: {}", uri, json);
            
            HttpRequest request = HttpRequest.builder()
                .uri(uri)
                .method("POST")
                .addHeader("Authorization", "Bearer " + apiToken)
                .addHeader("Content-Type", "application/json")
                .body(HttpRequest.JsonRequestBody.builder().content(requestBody).build())
                .build();

            return executeRequest(request, responseType);
        } catch (IOException e) {
            throw new CodaException("Failed to serialize request body: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a PUT request to the Coda API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param requestBody The request body object
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws CodaException if the request fails
     */
    public <T> T put(String endpoint, Object requestBody, Class<T> responseType) throws CodaException {
        URI uri = buildUri(endpoint);
        
        try {
            String json = MAPPER.writeValueAsString(requestBody);
            logger.debug("PUT {} with body: {}", uri, json);
            
            HttpRequest request = HttpRequest.builder()
                .uri(uri)
                .method("PUT")
                .addHeader("Authorization", "Bearer " + apiToken)
                .addHeader("Content-Type", "application/json")
                .body(HttpRequest.JsonRequestBody.builder().content(requestBody).build())
                .build();

            return executeRequest(request, responseType);
        } catch (IOException e) {
            throw new CodaException("Failed to serialize request body: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a DELETE request to the Coda API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws CodaException if the request fails
     */
    public <T> T delete(String endpoint, Class<T> responseType) throws CodaException {
        URI uri = buildUri(endpoint);
        HttpRequest request = HttpRequest.builder()
            .uri(uri)
            .method("DELETE")
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("Content-Type", "application/json")
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes an HTTP request and handles the response.
     */
    private <T> T executeRequest(HttpRequest request, Class<T> responseType) throws CodaException {
        try (HttpClient client = new HttpClient(runContext, options)) {
            HttpResponse<String> response = client.request(request, String.class);

            logger.debug("Response code: {}", response.getStatus().getCode());
            logger.debug("Response body: {}", response.getBody());

            if (responseType == Void.class || response.getBody() == null || response.getBody().isEmpty()) {
                return null;
            }

            return MAPPER.readValue(response.getBody(), responseType);
        } catch (HttpClientResponseException e) {
            handleErrorResponse(e);
            throw new CodaException("Unexpected error after handling", e);
        } catch (Exception e) {
            throw new CodaException("Failed to execute request: " + e.getMessage(), e);
        }
    }

    /**
     * Executes an HTTP request and handles the response with generic type support.
     */
    private <T> T executeRequest(HttpRequest request, TypeReference<T> typeReference) throws CodaException {
        try (HttpClient client = new HttpClient(runContext, options)) {
            HttpResponse<String> response = client.request(request, String.class);

            logger.debug("Response code: {}", response.getStatus().getCode());
            logger.debug("Response body: {}", response.getBody());

            if (response.getBody() == null || response.getBody().isEmpty()) {
                return null;
            }

            return MAPPER.readValue(response.getBody(), typeReference);
        } catch (HttpClientResponseException e) {
            handleErrorResponse(e);
            throw new CodaException("Unexpected error after handling", e);
        } catch (Exception e) {
            throw new CodaException("Failed to execute request: " + e.getMessage(), e);
        }
    }

    /**
     * Handles error responses from the Coda API.
     */
    private void handleErrorResponse(HttpClientResponseException e) throws CodaException {
        int statusCode = e.getResponse().getStatus().getCode();
        String responseBody = e.getResponse().getBody() != null ? e.getResponse().getBody().toString() : "";

        switch (statusCode) {
            case 401:
                throw new CodaAuthenticationException("Authentication failed. Please check your API token.");
            case 404:
                throw new CodaResourceNotFoundException("Resource not found: " + e.getResponse().getRequest().getUri());
            case 429:
                // Note: Retry-After header parsing would go here, but HttpHeaders API varies
                // For now, we use the default constructor
                throw new CodaRateLimitException("Rate limit exceeded");
            default:
                String message = String.format("Request failed with status %d: %s", statusCode, responseBody);
                throw new CodaException(message, statusCode);
        }
    }

    /**
     * Builds a full URI from an endpoint.
     */
    private URI buildUri(String endpoint) {
        if (endpoint.startsWith("http")) {
            return URI.create(endpoint); // Full URL provided (for pagination links)
        }

        String cleanEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return URI.create(BASE_URL + cleanEndpoint);
    }
}
