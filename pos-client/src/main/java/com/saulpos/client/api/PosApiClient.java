package com.saulpos.client.api;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import java.util.concurrent.CompletableFuture;

public interface PosApiClient {

    CompletableFuture<Boolean> ping();

    CompletableFuture<AuthTokenResponse> login(String username, String password);

    CompletableFuture<AuthTokenResponse> refresh(String refreshToken);

    CompletableFuture<CurrentUserResponse> currentUser();

    CompletableFuture<Void> logout();

    CompletableFuture<CashShiftResponse> openShift(CashShiftOpenRequest request);

    CompletableFuture<CashMovementResponse> addCashMovement(Long shiftId, CashMovementRequest request);

    CompletableFuture<CashShiftResponse> closeShift(Long shiftId, CashShiftCloseRequest request);

    CompletableFuture<CashShiftResponse> getShift(Long shiftId);

    void setAccessToken(String accessToken);
}
