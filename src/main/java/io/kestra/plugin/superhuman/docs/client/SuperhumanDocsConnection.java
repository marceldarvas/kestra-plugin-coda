package io.kestra.plugin.superhuman.docs.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kestra.plugin.superhuman.docs.exceptions.*;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Manages HTTP connections and communication with the Superhuman Docs API.
 */
public class SuperhumanDocsConnection {
    /**
     * Base URL of the Superhuman Docs (formerly Coda) REST API.
     * <p>
     * Coda was renamed to Superhuman Docs on 2026-07-08, but the official API reference
     * (https://docs.superhuman.com/developers/apis/v1, "Superhuman Docs API 1.6.0") still documents this base path,
     * and Superhuman's help center states that existing endpoints such as https://coda.io/apis/v1/docs
     * continue to work without changes.
     */
    static final String DEFAULT_BASE_URL = "https://coda.io/apis/v1";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final Logger logger;

    /**
     * Creates a new Superhuman Docs API connection.
     *
     * @param apiToken The Superhuman Docs API token for authentication
     * @param logger Logger instance for logging
     */
    public SuperhumanDocsConnection(String apiToken, Logger logger) {
        this(apiToken, logger, DEFAULT_BASE_URL);
    }

    /**
     * Creates a connection against a custom base URL. Package-private: only used by tests to point the client
     * at a mock server.
     */
    SuperhumanDocsConnection(String apiToken, Logger logger, String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.logger = logger;
        this.objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

        this.client = new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor(apiToken))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    /**
     * Executes a GET request to the Superhuman Docs API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws SuperhumanDocsException if the request fails
     */
    public <T> T get(String endpoint, Class<T> responseType) throws SuperhumanDocsException {
        String url = buildUrl(endpoint);
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes a POST request to the Superhuman Docs API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param requestBody The request body object
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws SuperhumanDocsException if the request fails
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws SuperhumanDocsException {
        String url = buildUrl(endpoint);
        String json;
        try {
            json = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new SuperhumanDocsException("Failed to serialize request body: " + e.getMessage(), e);
        }

        logger.debug("POST {} with body: {}", url, json);

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes a PUT request to the Superhuman Docs API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param requestBody The request body object
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws SuperhumanDocsException if the request fails
     */
    public <T> T put(String endpoint, Object requestBody, Class<T> responseType) throws SuperhumanDocsException {
        String url = buildUrl(endpoint);
        String json;
        try {
            json = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new SuperhumanDocsException("Failed to serialize request body: " + e.getMessage(), e);
        }

        logger.debug("PUT {} with body: {}", url, json);

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
            .url(url)
            .put(body)
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes a DELETE request to the Superhuman Docs API.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param responseType The class type to deserialize the response to
     * @return The deserialized response
     * @throws SuperhumanDocsException if the request fails
     */
    public <T> T delete(String endpoint, Class<T> responseType) throws SuperhumanDocsException {
        String url = buildUrl(endpoint);
        Request request = new Request.Builder()
            .url(url)
            .delete()
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes an HTTP request and handles the response.
     */
    private <T> T executeRequest(Request request, Class<T> responseType) throws SuperhumanDocsException {
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            logger.debug("Response code: {}", response.code());
            logger.debug("Response body: {}", responseBody);

            if (!response.isSuccessful()) {
                handleErrorResponse(response, responseBody);
            }

            if (responseType == Void.class || responseBody.isEmpty()) {
                return null;
            }

            return objectMapper.readValue(responseBody, responseType);
        } catch (IOException e) {
            throw new SuperhumanDocsException("Failed to execute request: " + e.getMessage(), e);
        }
    }

    /**
     * Handles error responses from the Superhuman Docs API.
     */
    private void handleErrorResponse(Response response, String responseBody) throws SuperhumanDocsException {
        int statusCode = response.code();

        switch (statusCode) {
            case 401:
                throw new SuperhumanDocsAuthenticationException("Authentication failed. Please check your API token.");
            case 404:
                throw new SuperhumanDocsResourceNotFoundException("Resource not found: " + response.request().url());
            case 429:
                String retryAfter = response.header("Retry-After");
                Integer retrySeconds = null;
                if (retryAfter != null) {
                    try {
                        retrySeconds = Integer.parseInt(retryAfter);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid Retry-After header value: {}", retryAfter);
                    }
                }
                throw new SuperhumanDocsRateLimitException("Rate limit exceeded", retrySeconds);
            default:
                String message = String.format("Request failed with status %d: %s", statusCode, responseBody);
                throw new SuperhumanDocsException(message, statusCode);
        }
    }

    /**
     * Builds a full URL from an endpoint.
     */
    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("http")) {
            return endpoint; // Full URL provided (for pagination links)
        }

        String cleanEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return baseUrl + cleanEndpoint;
    }

    /**
     * Interceptor that adds authentication header to all requests.
     */
    private static class AuthInterceptor implements Interceptor {
        private final String apiToken;

        public AuthInterceptor(String apiToken) {
            this.apiToken = apiToken;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .build();
            return chain.proceed(authenticated);
        }
    }
}
