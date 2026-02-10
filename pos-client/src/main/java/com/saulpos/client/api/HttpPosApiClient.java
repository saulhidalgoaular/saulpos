package com.saulpos.client.api;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.auth.LoginRequest;
import com.saulpos.api.auth.RefreshTokenRequest;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public final class HttpPosApiClient implements PosApiClient {

    private final URI baseUri;
    private final ObjectMapper objectMapper;
    private final Function<HttpRequest, CompletableFuture<HttpResponse<String>>> transport;
    private volatile String accessToken;

    public HttpPosApiClient(URI baseUri, ObjectMapper objectMapper) {
        this(baseUri, objectMapper, createDefaultTransport());
    }

    HttpPosApiClient(URI baseUri,
                     ObjectMapper objectMapper,
                     Function<HttpRequest, CompletableFuture<HttpResponse<String>>> transport) {
        this.baseUri = baseUri;
        this.objectMapper = objectMapper;
        this.transport = transport;
    }

    @Override
    public CompletableFuture<Boolean> ping() {
        HttpRequest request = baseRequest("/actuator/health")
                .GET()
                .build();

        return transport.apply(request)
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
    public CompletableFuture<AuthTokenResponse> login(String username, String password) {
        return postJson("/api/auth/login", new LoginRequest(username, password), AuthTokenResponse.class)
                .thenApply(response -> {
                    setAccessToken(response.accessToken());
                    return response;
                });
    }

    @Override
    public CompletableFuture<AuthTokenResponse> refresh(String refreshToken) {
        return postJson("/api/auth/refresh", new RefreshTokenRequest(refreshToken), AuthTokenResponse.class)
                .thenApply(response -> {
                    setAccessToken(response.accessToken());
                    return response;
                });
    }

    @Override
    public CompletableFuture<CurrentUserResponse> currentUser() {
        HttpRequest request = baseRequest("/api/security/me")
                .GET()
                .build();
        return send(request, CurrentUserResponse.class);
    }

    @Override
    public CompletableFuture<Void> logout() {
        HttpRequest request = baseRequest("/api/auth/logout")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return send(request, Void.class)
                .whenComplete((ignored, throwable) -> accessToken = null);
    }

    @Override
    public CompletableFuture<CashShiftResponse> openShift(CashShiftOpenRequest request) {
        return postJson("/api/shifts/open", request, CashShiftResponse.class);
    }

    @Override
    public CompletableFuture<CashMovementResponse> addCashMovement(Long shiftId, CashMovementRequest request) {
        return postJson("/api/shifts/" + shiftId + "/cash-movements", request, CashMovementResponse.class);
    }

    @Override
    public CompletableFuture<CashShiftResponse> closeShift(Long shiftId, CashShiftCloseRequest request) {
        return postJson("/api/shifts/" + shiftId + "/close", request, CashShiftResponse.class);
    }

    @Override
    public CompletableFuture<CashShiftResponse> getShift(Long shiftId) {
        HttpRequest request = baseRequest("/api/shifts/" + shiftId)
                .GET()
                .build();
        return send(request, CashShiftResponse.class);
    }

    @Override
    public CompletableFuture<ProductLookupResponse> lookupProductByBarcode(Long merchantId, String barcode) {
        String path = "/api/catalog/products/lookup?merchantId="
                + merchantId
                + "&barcode="
                + encodeQueryParam(barcode);
        HttpRequest request = baseRequest(path)
                .GET()
                .build();
        return send(request, ProductLookupResponse.class);
    }

    @Override
    public CompletableFuture<ProductSearchResponse> searchProducts(Long merchantId,
                                                                   String query,
                                                                   Boolean active,
                                                                   int page,
                                                                   int size) {
        StringBuilder path = new StringBuilder("/api/catalog/products/search?merchantId=")
                .append(merchantId)
                .append("&q=").append(encodeQueryParam(query))
                .append("&page=").append(page)
                .append("&size=").append(size);
        if (active != null) {
            path.append("&active=").append(active);
        }
        HttpRequest request = baseRequest(path.toString())
                .GET()
                .build();
        return send(request, ProductSearchResponse.class);
    }

    @Override
    public CompletableFuture<SaleCartResponse> createCart(SaleCartCreateRequest request) {
        return postJson("/api/sales/carts", request, SaleCartResponse.class);
    }

    @Override
    public CompletableFuture<SaleCartResponse> getCart(Long cartId) {
        HttpRequest request = baseRequest("/api/sales/carts/" + cartId)
                .GET()
                .build();
        return send(request, SaleCartResponse.class);
    }

    @Override
    public CompletableFuture<SaleCartResponse> addCartLine(Long cartId, SaleCartAddLineRequest request) {
        return postJson("/api/sales/carts/" + cartId + "/lines", request, SaleCartResponse.class);
    }

    @Override
    public CompletableFuture<SaleCartResponse> updateCartLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(request);
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
        HttpRequest httpRequest = baseRequest("/api/sales/carts/" + cartId + "/lines/" + lineId)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(httpRequest, SaleCartResponse.class);
    }

    @Override
    public CompletableFuture<SaleCartResponse> removeCartLine(Long cartId, Long lineId) {
        HttpRequest request = baseRequest("/api/sales/carts/" + cartId + "/lines/" + lineId)
                .DELETE()
                .build();
        return send(request, SaleCartResponse.class);
    }

    @Override
    public CompletableFuture<SaleCartResponse> recalculateCart(Long cartId) {
        HttpRequest request = baseRequest("/api/sales/carts/" + cartId + "/recalculate")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request, SaleCartResponse.class);
    }

    @Override
    public CompletableFuture<SaleCheckoutResponse> checkout(SaleCheckoutRequest request) {
        return postJsonWithIdempotency("/api/sales/checkout", request, SaleCheckoutResponse.class);
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private <T> CompletableFuture<T> postJson(String path, Object requestBody, Class<T> responseType) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(requestBody);
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }

        HttpRequest request = baseRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    private <T> CompletableFuture<T> postJsonWithIdempotency(String path, Object requestBody, Class<T> responseType) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(requestBody);
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }

        HttpRequest request = baseRequest(path)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "pos-client-" + UUID.randomUUID())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    private <T> CompletableFuture<T> send(HttpRequest request, Class<T> responseType) {
        return transport.apply(request)
                .thenApply(response -> {
                    if (response.statusCode() / 100 != 2) {
                        throw new CompletionException(toProblemException(response.statusCode(), response.body()));
                    }
                    if (responseType == Void.class || response.body() == null || response.body().isBlank()) {
                        return null;
                    }
                    try {
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (IOException ex) {
                        throw new CompletionException(ex);
                    }
                });
    }

    private ApiProblemException toProblemException(int status, String responseBody) {
        try {
            JsonNode body = objectMapper.readTree(responseBody == null ? "{}" : responseBody);
            String code = body.path("code").asText(null);
            String detail = body.path("detail").asText(null);
            return new ApiProblemException(status, code, detail);
        } catch (IOException ex) {
            return new ApiProblemException(status, null, "Request failed with status " + status);
        }
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

    private static Function<HttpRequest, CompletableFuture<HttpResponse<String>>> createDefaultTransport() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        return request -> httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private String encodeQueryParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
