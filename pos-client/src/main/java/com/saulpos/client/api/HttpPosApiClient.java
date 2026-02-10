package com.saulpos.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class HttpPosApiClient implements PosApiClient {

    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private volatile String accessToken;

    public HttpPosApiClient(URI baseUri, ObjectMapper objectMapper) {
        this.baseUri = baseUri;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public CompletableFuture<Boolean> ping() {
        HttpRequest request = baseRequest("/actuator/health")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() / 100 != 2) {
                        return false;
                    }
                    try {
                        JsonNode body = objectMapper.readTree(response.body());
                        return "UP".equalsIgnoreCase(body.path("status").asText());
                    } catch (IOException ex) {
                        throw new CompletionException(ex);
                    }
                });
    }

    @Override
    public CompletableFuture<Void> login(String username, String password) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Auth UI contract is introduced in O2")
        );
    }

    @Override
    public CompletableFuture<Void> logout() {
        accessToken = null;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private HttpRequest.Builder baseRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(baseUri.resolve(path))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10));
        if (accessToken != null && !accessToken.isBlank()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }
}
