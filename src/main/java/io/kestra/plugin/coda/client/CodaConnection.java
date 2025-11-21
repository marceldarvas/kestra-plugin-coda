package io.kestra.plugin.coda.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.kestra.plugin.coda.exceptions.*;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * Manages HTTP connections and communication with the Coda API.
 */
public class CodaConnection {
    private static final String BASE_URL = "https://coda.io/apis/v1";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final Logger logger;

    /**
     * Creates a new Coda API connection.
     *
     * @param apiToken The Coda API token for authentication
     * @param logger Logger instance for logging
     */
    public CodaConnection(String apiToken, Logger logger) {
        this.logger = logger;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        this.client = new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor(apiToken))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
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
        String url = buildUrl(endpoint);
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes a GET request to the Coda API with generic type support.
     *
     * @param endpoint The API endpoint (relative to base URL)
     * @param typeToken The TypeToken for generic types
     * @return The deserialized response
     * @throws CodaException if the request fails
     */
    public <T> T get(String endpoint, TypeToken<T> typeToken) throws CodaException {
        String url = buildUrl(endpoint);
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();

        return executeRequest(request, typeToken.getType());
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
        String url = buildUrl(endpoint);
        String json = gson.toJson(requestBody);

        logger.debug("POST {} with body: {}", url, json);

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        return executeRequest(request, responseType);
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
        String url = buildUrl(endpoint);
        String json = gson.toJson(requestBody);

        logger.debug("PUT {} with body: {}", url, json);

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
            .url(url)
            .put(body)
            .build();

        return executeRequest(request, responseType);
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
    private <T> T executeRequest(Request request, Class<T> responseType) throws CodaException {
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

            return gson.fromJson(responseBody, responseType);
        } catch (IOException e) {
            throw new CodaException("Failed to execute request: " + e.getMessage(), e);
        }
    }

    /**
     * Executes an HTTP request and handles the response with generic type support.
     */
    private <T> T executeRequest(Request request, Type type) throws CodaException {
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            logger.debug("Response code: {}", response.code());
            logger.debug("Response body: {}", responseBody);

            if (!response.isSuccessful()) {
                handleErrorResponse(response, responseBody);
            }

            if (responseBody.isEmpty()) {
                return null;
            }

            return gson.fromJson(responseBody, type);
        } catch (IOException e) {
            throw new CodaException("Failed to execute request: " + e.getMessage(), e);
        }
    }

    /**
     * Handles error responses from the Coda API.
     */
    private void handleErrorResponse(Response response, String responseBody) throws CodaException {
        int statusCode = response.code();

        switch (statusCode) {
            case 401:
                throw new CodaAuthenticationException("Authentication failed. Please check your API token.");
            case 404:
                throw new CodaResourceNotFoundException("Resource not found: " + response.request().url());
            case 429:
                String retryAfter = response.header("Retry-After");
                Integer retrySeconds = retryAfter != null ? Integer.parseInt(retryAfter) : null;
                throw new CodaRateLimitException("Rate limit exceeded", retrySeconds);
            default:
                String message = String.format("Request failed with status %d: %s", statusCode, responseBody);
                throw new CodaException(message, statusCode);
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
        return BASE_URL + cleanEndpoint;
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
