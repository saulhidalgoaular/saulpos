package com.saulpos.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementType;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
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
