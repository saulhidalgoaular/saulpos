package com.saulpos.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saulpos.api.refund.SaleReturnSubmitLineRequest;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementType;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.tax.TenderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpPosApiClientTest {

    @Test
    void loginCurrentUserAndLogout_shouldUseExpectedContracts() {
        List<HttpRequest> requests = new ArrayList<>();

        HttpPosApiClient client = new HttpPosApiClient(
                URI.create("http://localhost:8080"),
                new ObjectMapper().registerModule(new JavaTimeModule()),
                request -> {
                    requests.add(request);
                    String path = request.uri().getPath();
                    if ("/api/auth/login".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"accessToken\":\"access-123\",\"refreshToken\":\"refresh-123\","
                                        + "\"accessTokenExpiresAt\":\"2026-02-10T12:15:00Z\","
                                        + "\"refreshTokenExpiresAt\":\"2026-02-10T18:15:00Z\","
                                        + "\"roles\":[\"CASHIER\"]}"
                        ));
                    }
                    if ("/api/security/me".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"userId\":11,\"username\":\"cashier\",\"roles\":[\"CASHIER\"]}"
                        ));
                    }
                    if ("/api/auth/logout".equals(path)) {
                        return CompletableFuture.completedFuture(response(request, 204, ""));
                    }
                    return CompletableFuture.completedFuture(response(request, 404, ""));
                }
        );

        assertEquals("access-123", client.login("cashier", "secret").join().accessToken());
        assertEquals("cashier", client.currentUser().join().username());
        client.logout().join();

        HttpRequest meRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/security/me")).findFirst().orElseThrow();
        HttpRequest logoutRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/auth/logout")).findFirst().orElseThrow();

        assertEquals("Bearer access-123", meRequest.headers().firstValue("Authorization").orElseThrow());
        assertEquals("Bearer access-123", logoutRequest.headers().firstValue("Authorization").orElseThrow());
    }

    @Test
    void loginProblemResponse_shouldMapToApiProblemException() {
        HttpPosApiClient client = new HttpPosApiClient(
                URI.create("http://localhost:8080"),
                new ObjectMapper().registerModule(new JavaTimeModule()),
                request -> CompletableFuture.completedFuture(response(
                        request,
                        401,
                        "{\"title\":\"Invalid username or password\","
                                + "\"detail\":\"Invalid username or password\","
                                + "\"code\":\"POS-4011\"}"
                ))
        );

        CompletionException ex = assertThrows(CompletionException.class,
                () -> client.login("cashier", "bad").join());
        assertTrue(ex.getCause() instanceof ApiProblemException);
        ApiProblemException problem = (ApiProblemException) ex.getCause();
        assertEquals("POS-4011", problem.code());
        assertEquals(401, problem.status());
    }

    @Test
    void shiftFlow_shouldUseExpectedContractsAndAuthorizationHeader() {
        List<HttpRequest> requests = new ArrayList<>();
        HttpPosApiClient client = new HttpPosApiClient(
                URI.create("http://localhost:8080"),
                new ObjectMapper().registerModule(new JavaTimeModule()),
                request -> {
                    requests.add(request);
                    String path = request.uri().getPath();
                    if ("/api/auth/login".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"accessToken\":\"access-123\",\"refreshToken\":\"refresh-123\","
                                        + "\"accessTokenExpiresAt\":\"2026-02-10T12:15:00Z\","
                                        + "\"refreshTokenExpiresAt\":\"2026-02-10T18:15:00Z\","
                                        + "\"roles\":[\"CASHIER\"]}"
                        ));
                    }
                    if ("/api/shifts/open".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                201,
                                "{\"id\":44,\"cashierUserId\":7,\"terminalDeviceId\":3,\"storeLocationId\":1,"
                                        + "\"status\":\"OPEN\",\"openingCash\":120.00,\"totalPaidIn\":0.00,\"totalPaidOut\":0.00,"
                                        + "\"expectedCloseCash\":120.00,\"countedCloseCash\":null,\"varianceCash\":null,"
                                        + "\"openedAt\":\"2026-02-10T12:00:00Z\",\"closedAt\":null}"
                        ));
                    }
                    if ("/api/shifts/44/cash-movements".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                201,
                                "{\"id\":10,\"shiftId\":44,\"movementType\":\"PAID_IN\",\"amount\":10.00,\"note\":\"cash top-up\","
                                        + "\"occurredAt\":\"2026-02-10T12:03:00Z\"}"
                        ));
                    }
                    if ("/api/shifts/44/close".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"id\":44,\"cashierUserId\":7,\"terminalDeviceId\":3,\"storeLocationId\":1,"
                                        + "\"status\":\"CLOSED\",\"openingCash\":120.00,\"totalPaidIn\":10.00,\"totalPaidOut\":0.00,"
                                        + "\"expectedCloseCash\":130.00,\"countedCloseCash\":129.00,\"varianceCash\":-1.00,"
                                        + "\"openedAt\":\"2026-02-10T12:00:00Z\",\"closedAt\":\"2026-02-10T20:00:00Z\"}"
                        ));
                    }
                    if ("/api/shifts/44".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"id\":44,\"cashierUserId\":7,\"terminalDeviceId\":3,\"storeLocationId\":1,"
                                        + "\"status\":\"OPEN\",\"openingCash\":120.00,\"totalPaidIn\":10.00,\"totalPaidOut\":0.00,"
                                        + "\"expectedCloseCash\":130.00,\"countedCloseCash\":null,\"varianceCash\":null,"
                                        + "\"openedAt\":\"2026-02-10T12:00:00Z\",\"closedAt\":null}"
                        ));
                    }
                    return CompletableFuture.completedFuture(response(request, 404, ""));
                }
        );

        client.login("cashier", "secret").join();
        assertEquals(44L, client.openShift(new CashShiftOpenRequest(7L, 3L, BigDecimal.valueOf(120))).join().id());
        assertEquals(CashMovementType.PAID_IN, client.addCashMovement(
                44L,
                new CashMovementRequest(CashMovementType.PAID_IN, BigDecimal.TEN, "cash top-up")
        ).join().movementType());
        assertEquals(44L, client.getShift(44L).join().id());
        assertEquals(BigDecimal.valueOf(-1.00).setScale(2), client.closeShift(
                44L,
                new CashShiftCloseRequest(BigDecimal.valueOf(129), "reconcile")
        ).join().varianceCash());

        HttpRequest openRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/shifts/open")).findFirst().orElseThrow();
        HttpRequest movementRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/shifts/44/cash-movements")).findFirst().orElseThrow();
        HttpRequest closeRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/shifts/44/close")).findFirst().orElseThrow();
        HttpRequest getRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/shifts/44") && req.method().equals("GET")).findFirst().orElseThrow();

        assertEquals("Bearer access-123", openRequest.headers().firstValue("Authorization").orElseThrow());
        assertEquals("Bearer access-123", movementRequest.headers().firstValue("Authorization").orElseThrow());
        assertEquals("Bearer access-123", closeRequest.headers().firstValue("Authorization").orElseThrow());
        assertEquals("Bearer access-123", getRequest.headers().firstValue("Authorization").orElseThrow());
    }

    @Test
    void sellContracts_shouldUseLookupSearchAndCartEndpoints() {
        List<HttpRequest> requests = new ArrayList<>();
        HttpPosApiClient client = new HttpPosApiClient(
                URI.create("http://localhost:8080"),
                new ObjectMapper().registerModule(new JavaTimeModule()),
                request -> {
                    requests.add(request);
                    String path = request.uri().getPath();
                    String query = request.uri().getRawQuery();
                    if ("/api/auth/login".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"accessToken\":\"access-123\",\"refreshToken\":\"refresh-123\","
                                        + "\"accessTokenExpiresAt\":\"2026-02-10T12:15:00Z\","
                                        + "\"refreshTokenExpiresAt\":\"2026-02-10T18:15:00Z\","
                                        + "\"roles\":[\"CASHIER\"]}"
                        ));
                    }
                    if ("/api/catalog/products/lookup".equals(path) && query.contains("barcode=775123456")) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"productId\":301,\"variantId\":null,\"merchantId\":1,\"sku\":\"SODA-350\","
                                        + "\"productName\":\"Soda 350ml\",\"variantCode\":null,\"variantName\":null,"
                                        + "\"barcode\":\"775123456\",\"saleMode\":\"UNIT\",\"quantityUom\":\"UNIT\",\"quantityPrecision\":0}"
                        ));
                    }
                    if ("/api/catalog/products/search".equals(path) && query.contains("q=soda")) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"items\":[{\"id\":301,\"merchantId\":1,\"categoryId\":null,\"taxGroupId\":null,"
                                        + "\"sku\":\"SODA-350\",\"name\":\"Soda 350ml\",\"basePrice\":1.50,\"saleMode\":\"UNIT\","
                                        + "\"quantityUom\":\"UNIT\",\"quantityPrecision\":0,\"openPriceMin\":null,\"openPriceMax\":null,"
                                        + "\"openPriceRequiresReason\":false,\"lotTrackingEnabled\":false,\"description\":null,"
                                        + "\"active\":true,\"variants\":[]}],\"page\":0,\"size\":12,\"totalElements\":1,"
                                        + "\"totalPages\":1,\"hasNext\":false,\"hasPrevious\":false}"
                        ));
                    }
                    if ("/api/sales/carts".equals(path) && "POST".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(request, 201, cartJson()));
                    }
                    if ("/api/sales/carts/44".equals(path) && "GET".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(request, 200, cartJson()));
                    }
                    if ("/api/sales/carts/44/lines".equals(path) && "POST".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(request, 200, cartJson()));
                    }
                    if ("/api/sales/carts/44/lines/11".equals(path) && "PUT".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(request, 200, cartJson()));
                    }
                    if ("/api/sales/carts/44/lines/11".equals(path) && "DELETE".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(request, 200, cartJson()));
                    }
                    if ("/api/sales/carts/44/recalculate".equals(path) && "POST".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(request, 200, cartJson()));
                    }
                    if ("/api/sales/checkout".equals(path) && "POST".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"cartId\":44,\"saleId\":501,\"receiptNumber\":\"R-0000501\",\"paymentId\":88,"
                                        + "\"paymentStatus\":\"CAPTURED\",\"totalPayable\":10.00,\"totalAllocated\":10.00,"
                                        + "\"totalTendered\":10.00,\"changeAmount\":0.00,\"payments\":["
                                        + "{\"sequenceNumber\":1,\"tenderType\":\"CASH\",\"amount\":10.00,"
                                        + "\"tenderedAmount\":10.00,\"changeAmount\":0.00,\"reference\":null}],"
                                        + "\"capturedAt\":\"2026-02-10T12:05:00Z\"}"
                        ));
                    }
                    return CompletableFuture.completedFuture(response(request, 404, ""));
                }
        );

        client.login("cashier", "secret").join();
        assertEquals(301L, client.lookupProductByBarcode(1L, "775123456").join().productId());
        assertEquals(1, client.searchProducts(1L, "soda", true, 0, 12).join().items().size());
        assertEquals(44L, client.createCart(new SaleCartCreateRequest(7L, 1L, 3L, java.time.Instant.parse("2026-02-10T12:00:00Z"))).join().id());
        assertEquals(44L, client.getCart(44L).join().id());
        assertEquals(44L, client.addCartLine(44L, new SaleCartAddLineRequest("line-1", 301L, BigDecimal.ONE, null, null)).join().id());
        assertEquals(44L, client.updateCartLine(44L, 11L, new SaleCartUpdateLineRequest(BigDecimal.ONE, null, null)).join().id());
        assertEquals(44L, client.removeCartLine(44L, 11L).join().id());
        assertEquals(44L, client.recalculateCart(44L).join().id());
        assertEquals("R-0000501", client.checkout(new SaleCheckoutRequest(
                44L,
                7L,
                3L,
                List.of(new SaleCheckoutPaymentRequest(TenderType.CASH, BigDecimal.TEN, BigDecimal.TEN, null))
        )).join().receiptNumber());

        HttpRequest lookupRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/catalog/products/lookup")).findFirst().orElseThrow();
        HttpRequest searchRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/catalog/products/search")).findFirst().orElseThrow();
        HttpRequest addLineRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/sales/carts/44/lines")).findFirst().orElseThrow();
        HttpRequest checkoutRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/sales/checkout")).findFirst().orElseThrow();

        assertTrue(lookupRequest.uri().getRawQuery().contains("merchantId=1"));
        assertTrue(searchRequest.uri().getRawQuery().contains("q=soda"));
        assertEquals("Bearer access-123", addLineRequest.headers().firstValue("Authorization").orElseThrow());
        assertTrue(checkoutRequest.headers().firstValue("Idempotency-Key").orElseThrow().startsWith("pos-client-"));
    }

    @Test
    void refundContracts_shouldUseLookupAndSubmitEndpoints() {
        List<HttpRequest> requests = new ArrayList<>();
        HttpPosApiClient client = new HttpPosApiClient(
                URI.create("http://localhost:8080"),
                new ObjectMapper().registerModule(new JavaTimeModule()),
                request -> {
                    requests.add(request);
                    String path = request.uri().getPath();
                    String query = request.uri().getRawQuery();
                    if ("/api/auth/login".equals(path)) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"accessToken\":\"access-123\",\"refreshToken\":\"refresh-123\","
                                        + "\"accessTokenExpiresAt\":\"2026-02-10T12:15:00Z\","
                                        + "\"refreshTokenExpiresAt\":\"2026-02-10T18:15:00Z\","
                                        + "\"roles\":[\"CASHIER\"]}"
                        ));
                    }
                    if ("/api/refunds/lookup".equals(path) && query.contains("receiptNumber=R-0000501")) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"saleId\":501,\"receiptNumber\":\"R-0000501\",\"storeLocationId\":1,"
                                        + "\"terminalDeviceId\":3,\"soldAt\":\"2026-02-10T11:00:00Z\",\"lines\":["
                                        + "{\"saleLineId\":1001,\"productId\":301,\"lineNumber\":1,\"quantitySold\":2.000,"
                                        + "\"quantityReturned\":1.000,\"quantityAvailable\":1.000,\"unitPrice\":2.50,\"grossAmount\":5.00}]}"
                        ));
                    }
                    if ("/api/refunds/submit".equals(path) && "POST".equals(request.method())) {
                        return CompletableFuture.completedFuture(response(
                                request,
                                200,
                                "{\"saleReturnId\":9001,\"saleId\":501,\"receiptNumber\":\"R-0000501\","
                                        + "\"returnReference\":\"RET-SALE-501\",\"reasonCode\":\"DAMAGED\","
                                        + "\"refundTenderType\":\"CASH\",\"subtotalNet\":2.12,\"totalTax\":0.38,\"totalGross\":2.50,"
                                        + "\"lines\":[{\"saleReturnLineId\":1,\"saleLineId\":1001,\"productId\":301,\"lineNumber\":1,"
                                        + "\"quantity\":1.000,\"netAmount\":2.12,\"taxAmount\":0.38,\"grossAmount\":2.50}],"
                                        + "\"createdAt\":\"2026-02-10T12:05:00Z\"}"
                        ));
                    }
                    return CompletableFuture.completedFuture(response(request, 404, ""));
                }
        );

        client.login("cashier", "secret").join();
        assertEquals(501L, client.lookupReturnByReceipt("R-0000501").join().saleId());
        assertEquals("RET-SALE-501", client.submitReturn(new SaleReturnSubmitRequest(
                501L,
                "R-0000501",
                "DAMAGED",
                TenderType.CASH,
                null,
                "can dent",
                List.of(new SaleReturnSubmitLineRequest(1001L, BigDecimal.ONE))
        )).join().returnReference());

        HttpRequest lookupRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/refunds/lookup")).findFirst().orElseThrow();
        HttpRequest submitRequest = requests.stream().filter(req -> req.uri().getPath().equals("/api/refunds/submit")).findFirst().orElseThrow();
        assertTrue(lookupRequest.uri().getRawQuery().contains("receiptNumber=R-0000501"));
        assertEquals("Bearer access-123", submitRequest.headers().firstValue("Authorization").orElseThrow());
    }

    private static String cartJson() {
        return "{\"id\":44,\"cashierUserId\":7,\"storeLocationId\":1,\"terminalDeviceId\":3,"
                + "\"status\":\"ACTIVE\",\"pricingAt\":\"2026-02-10T12:00:00Z\",\"lines\":[],"
                + "\"subtotalNet\":0.00,\"totalTax\":0.00,\"totalGross\":0.00,"
                + "\"roundingAdjustment\":0.00,\"totalPayable\":0.00,\"rounding\":null,"
                + "\"createdAt\":\"2026-02-10T12:00:00Z\",\"updatedAt\":\"2026-02-10T12:00:00Z\"}";
    }

    private HttpResponse<String> response(HttpRequest request, int status, String body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public HttpRequest request() {
                return request;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (name, value) -> true);
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<javax.net.ssl.SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return request.uri();
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }
}
