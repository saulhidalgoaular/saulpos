package com.saulpos.client.api;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.auth.LoginRequest;
import com.saulpos.api.auth.RefreshTokenRequest;
import com.saulpos.api.catalog.PriceResolutionResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductRequest;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.api.customer.CustomerRequest;
import com.saulpos.api.customer.CustomerResponse;
import com.saulpos.api.refund.SaleReturnLookupResponse;
import com.saulpos.api.refund.SaleReturnResponse;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.report.CashShiftReportResponse;
import com.saulpos.api.report.EndOfDayCashReportResponse;
import com.saulpos.api.report.ExceptionReportEventType;
import com.saulpos.api.report.ExceptionReportResponse;
import com.saulpos.api.report.InventoryLowStockReportResponse;
import com.saulpos.api.report.InventoryMovementReportResponse;
import com.saulpos.api.report.InventoryStockOnHandReportResponse;
import com.saulpos.api.receipt.CashDrawerOpenRequest;
import com.saulpos.api.receipt.CashDrawerOpenResponse;
import com.saulpos.api.receipt.ReceiptPrintRequest;
import com.saulpos.api.receipt.ReceiptPrintResponse;
import com.saulpos.api.report.SalesReturnsReportResponse;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.security.CurrentUserPermissionsResponse;
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
import java.time.Instant;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.List;
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
    public CompletableFuture<List<ProductResponse>> listProducts(Long merchantId, Boolean active, String query) {
        StringBuilder path = new StringBuilder("/api/catalog/products");
        if (merchantId != null || active != null || query != null) {
            path.append("?");
            boolean hasParam = false;
            if (merchantId != null) {
                path.append("merchantId=").append(merchantId);
                hasParam = true;
            }
            if (active != null) {
                if (hasParam) {
                    path.append("&");
                }
                path.append("active=").append(active);
                hasParam = true;
            }
            if (query != null && !query.isBlank()) {
                if (hasParam) {
                    path.append("&");
                }
                path.append("q=").append(encodeQueryParam(query));
            }
        }
        HttpRequest request = baseRequest(path.toString())
                .GET()
                .build();
        return sendList(request, ProductResponse.class);
    }

    @Override
    public CompletableFuture<ProductResponse> createProduct(ProductRequest request) {
        return postJson("/api/catalog/products", request, ProductResponse.class);
    }

    @Override
    public CompletableFuture<ProductResponse> updateProduct(Long productId, ProductRequest request) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(request);
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
        HttpRequest httpRequest = baseRequest("/api/catalog/products/" + productId)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(httpRequest, ProductResponse.class);
    }

    @Override
    public CompletableFuture<PriceResolutionResponse> resolvePrice(Long storeLocationId, Long productId, Long customerId) {
        StringBuilder path = new StringBuilder("/api/catalog/prices/resolve?storeLocationId=")
                .append(storeLocationId)
                .append("&productId=").append(productId);
        if (customerId != null) {
            path.append("&customerId=").append(customerId);
        }
        HttpRequest request = baseRequest(path.toString())
                .GET()
                .build();
        return send(request, PriceResolutionResponse.class);
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
    public CompletableFuture<SaleReturnLookupResponse> lookupReturnByReceipt(String receiptNumber) {
        String path = "/api/refunds/lookup?receiptNumber=" + encodeQueryParam(receiptNumber);
        HttpRequest request = baseRequest(path)
                .GET()
                .build();
        return send(request, SaleReturnLookupResponse.class);
    }

    @Override
    public CompletableFuture<SaleReturnResponse> submitReturn(SaleReturnSubmitRequest request) {
        return postJson("/api/refunds/submit", request, SaleReturnResponse.class);
    }

    @Override
    public CompletableFuture<List<CustomerResponse>> listCustomers(Long merchantId, Boolean active) {
        StringBuilder path = new StringBuilder("/api/customers");
        if (merchantId != null || active != null) {
            path.append("?");
            boolean hasParam = false;
            if (merchantId != null) {
                path.append("merchantId=").append(merchantId);
                hasParam = true;
            }
            if (active != null) {
                if (hasParam) {
                    path.append("&");
                }
                path.append("active=").append(active);
            }
        }
        HttpRequest request = baseRequest(path.toString())
                .GET()
                .build();
        return sendList(request, CustomerResponse.class);
    }

    @Override
    public CompletableFuture<List<CustomerResponse>> lookupCustomers(Long merchantId,
                                                                     String documentType,
                                                                     String documentValue,
                                                                     String email,
                                                                     String phone) {
        StringBuilder path = new StringBuilder("/api/customers/lookup?merchantId=").append(merchantId);
        if (documentType != null && !documentType.isBlank()) {
            path.append("&documentType=").append(encodeQueryParam(documentType));
        }
        if (documentValue != null && !documentValue.isBlank()) {
            path.append("&documentValue=").append(encodeQueryParam(documentValue));
        }
        if (email != null && !email.isBlank()) {
            path.append("&email=").append(encodeQueryParam(email));
        }
        if (phone != null && !phone.isBlank()) {
            path.append("&phone=").append(encodeQueryParam(phone));
        }
        HttpRequest request = baseRequest(path.toString())
                .GET()
                .build();
        return sendList(request, CustomerResponse.class);
    }

    @Override
    public CompletableFuture<CustomerResponse> createCustomer(CustomerRequest request) {
        return postJson("/api/customers", request, CustomerResponse.class);
    }

    @Override
    public CompletableFuture<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(request);
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
        HttpRequest httpRequest = baseRequest("/api/customers/" + customerId)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(httpRequest, CustomerResponse.class);
    }

    @Override
    public CompletableFuture<CurrentUserPermissionsResponse> currentUserPermissions() {
        HttpRequest request = baseRequest("/api/security/permissions/current")
                .GET()
                .build();
        return send(request, CurrentUserPermissionsResponse.class);
    }

    @Override
    public CompletableFuture<ReceiptPrintResponse> printReceipt(ReceiptPrintRequest request) {
        return postJson("/api/receipts/print", request, ReceiptPrintResponse.class);
    }

    @Override
    public CompletableFuture<CashDrawerOpenResponse> openCashDrawer(CashDrawerOpenRequest request) {
        return postJson("/api/receipts/drawer/open", request, CashDrawerOpenResponse.class);
    }

    @Override
    public CompletableFuture<SalesReturnsReportResponse> getSalesReturnsReport(Instant from,
                                                                               Instant to,
                                                                               Long storeLocationId,
                                                                               Long terminalDeviceId,
                                                                               Long cashierUserId,
                                                                               Long categoryId,
                                                                               Long taxGroupId) {
        StringBuilder path = new StringBuilder("/api/reports/sales");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        hasParam = appendParam(path, "cashierUserId", cashierUserId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        appendParam(path, "taxGroupId", taxGroupId, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, SalesReturnsReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportSalesReturnsReportCsv(Instant from,
                                                                 Instant to,
                                                                 Long storeLocationId,
                                                                 Long terminalDeviceId,
                                                                 Long cashierUserId,
                                                                 Long categoryId,
                                                                 Long taxGroupId) {
        StringBuilder path = new StringBuilder("/api/reports/sales/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        hasParam = appendParam(path, "cashierUserId", cashierUserId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        appendParam(path, "taxGroupId", taxGroupId, hasParam);
        return getCsv(path.toString());
    }

    @Override
    public CompletableFuture<InventoryStockOnHandReportResponse> getInventoryStockOnHandReport(Long storeLocationId,
                                                                                                 Long categoryId,
                                                                                                 Long supplierId) {
        StringBuilder path = new StringBuilder("/api/reports/inventory/stock-on-hand");
        boolean hasParam = false;
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        appendParam(path, "supplierId", supplierId, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, InventoryStockOnHandReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportInventoryStockOnHandReportCsv(Long storeLocationId,
                                                                          Long categoryId,
                                                                          Long supplierId) {
        StringBuilder path = new StringBuilder("/api/reports/inventory/stock-on-hand/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        appendParam(path, "supplierId", supplierId, hasParam);
        return getCsv(path.toString());
    }

    @Override
    public CompletableFuture<InventoryLowStockReportResponse> getInventoryLowStockReport(Long storeLocationId,
                                                                                          Long categoryId,
                                                                                          Long supplierId,
                                                                                          BigDecimal minimumQuantity) {
        StringBuilder path = new StringBuilder("/api/reports/inventory/low-stock");
        boolean hasParam = false;
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        hasParam = appendParam(path, "supplierId", supplierId, hasParam);
        appendParam(path, "minimumQuantity", minimumQuantity, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, InventoryLowStockReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportInventoryLowStockReportCsv(Long storeLocationId,
                                                                       Long categoryId,
                                                                       Long supplierId,
                                                                       BigDecimal minimumQuantity) {
        StringBuilder path = new StringBuilder("/api/reports/inventory/low-stock/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        hasParam = appendParam(path, "supplierId", supplierId, hasParam);
        appendParam(path, "minimumQuantity", minimumQuantity, hasParam);
        return getCsv(path.toString());
    }

    @Override
    public CompletableFuture<InventoryMovementReportResponse> getInventoryMovementReport(Instant from,
                                                                                          Instant to,
                                                                                          Long storeLocationId,
                                                                                          Long categoryId,
                                                                                          Long supplierId) {
        StringBuilder path = new StringBuilder("/api/reports/inventory/movements");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        appendParam(path, "supplierId", supplierId, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, InventoryMovementReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportInventoryMovementReportCsv(Instant from,
                                                                      Instant to,
                                                                      Long storeLocationId,
                                                                      Long categoryId,
                                                                      Long supplierId) {
        StringBuilder path = new StringBuilder("/api/reports/inventory/movements/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "categoryId", categoryId, hasParam);
        appendParam(path, "supplierId", supplierId, hasParam);
        return getCsv(path.toString());
    }

    @Override
    public CompletableFuture<CashShiftReportResponse> getCashShiftReport(Instant from,
                                                                          Instant to,
                                                                          Long storeLocationId,
                                                                          Long terminalDeviceId,
                                                                          Long cashierUserId) {
        StringBuilder path = new StringBuilder("/api/reports/cash/shifts");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        appendParam(path, "cashierUserId", cashierUserId, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, CashShiftReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportCashShiftReportCsv(Instant from,
                                                               Instant to,
                                                               Long storeLocationId,
                                                               Long terminalDeviceId,
                                                               Long cashierUserId) {
        StringBuilder path = new StringBuilder("/api/reports/cash/shifts/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        appendParam(path, "cashierUserId", cashierUserId, hasParam);
        return getCsv(path.toString());
    }

    @Override
    public CompletableFuture<EndOfDayCashReportResponse> getEndOfDayCashReport(Instant from,
                                                                                Instant to,
                                                                                Long storeLocationId,
                                                                                Long terminalDeviceId,
                                                                                Long cashierUserId) {
        StringBuilder path = new StringBuilder("/api/reports/cash/end-of-day");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        appendParam(path, "cashierUserId", cashierUserId, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, EndOfDayCashReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportEndOfDayCashReportCsv(Instant from,
                                                                  Instant to,
                                                                  Long storeLocationId,
                                                                  Long terminalDeviceId,
                                                                  Long cashierUserId) {
        StringBuilder path = new StringBuilder("/api/reports/cash/end-of-day/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        appendParam(path, "cashierUserId", cashierUserId, hasParam);
        return getCsv(path.toString());
    }

    @Override
    public CompletableFuture<ExceptionReportResponse> getExceptionReport(Instant from,
                                                                         Instant to,
                                                                         Long storeLocationId,
                                                                         Long terminalDeviceId,
                                                                         Long cashierUserId,
                                                                         String reasonCode,
                                                                         ExceptionReportEventType eventType) {
        StringBuilder path = new StringBuilder("/api/reports/exceptions");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        hasParam = appendParam(path, "cashierUserId", cashierUserId, hasParam);
        hasParam = appendParam(path, "reasonCode", reasonCode, hasParam);
        appendParam(path, "eventType", eventType, hasParam);
        HttpRequest request = baseRequest(path.toString()).GET().build();
        return send(request, ExceptionReportResponse.class);
    }

    @Override
    public CompletableFuture<String> exportExceptionReportCsv(Instant from,
                                                              Instant to,
                                                              Long storeLocationId,
                                                              Long terminalDeviceId,
                                                              Long cashierUserId,
                                                              String reasonCode,
                                                              ExceptionReportEventType eventType) {
        StringBuilder path = new StringBuilder("/api/reports/exceptions/export");
        boolean hasParam = false;
        hasParam = appendParam(path, "from", from, hasParam);
        hasParam = appendParam(path, "to", to, hasParam);
        hasParam = appendParam(path, "storeLocationId", storeLocationId, hasParam);
        hasParam = appendParam(path, "terminalDeviceId", terminalDeviceId, hasParam);
        hasParam = appendParam(path, "cashierUserId", cashierUserId, hasParam);
        hasParam = appendParam(path, "reasonCode", reasonCode, hasParam);
        appendParam(path, "eventType", eventType, hasParam);
        return getCsv(path.toString());
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
                .setHeader("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "pos-client-" + UUID.randomUUID())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    private CompletableFuture<String> getCsv(String path) {
        HttpRequest request = baseRequest(path)
                .setHeader("Accept", "text/csv")
                .GET()
                .build();
        return sendText(request);
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

    private <T> CompletableFuture<List<T>> sendList(HttpRequest request, Class<T> elementType) {
        return transport.apply(request)
                .thenApply(response -> {
                    if (response.statusCode() / 100 != 2) {
                        throw new CompletionException(toProblemException(response.statusCode(), response.body()));
                    }
                    if (response.body() == null || response.body().isBlank()) {
                        return List.of();
                    }
                    try {
                        return objectMapper.readValue(
                                response.body(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
                        );
                    } catch (IOException ex) {
                        throw new CompletionException(ex);
                    }
                });
    }

    private CompletableFuture<String> sendText(HttpRequest request) {
        return transport.apply(request)
                .thenApply(response -> {
                    if (response.statusCode() / 100 != 2) {
                        throw new CompletionException(toProblemException(response.statusCode(), response.body()));
                    }
                    return response.body() == null ? "" : response.body();
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

    private boolean appendParam(StringBuilder path, String name, Object value, boolean hasParam) {
        if (value == null) {
            return hasParam;
        }
        path.append(hasParam ? "&" : "?")
                .append(name)
                .append("=")
                .append(encodeQueryParam(toQueryValue(value)));
        return true;
    }

    private String toQueryValue(Object value) {
        if (value instanceof Instant instant) {
            return instant.toString();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        return value.toString();
    }
}
