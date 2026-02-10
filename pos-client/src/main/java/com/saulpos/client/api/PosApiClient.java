package com.saulpos.client.api;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
import java.util.concurrent.CompletableFuture;

public interface PosApiClient {

    CompletableFuture<Boolean> ping();

    CompletableFuture<AuthTokenResponse> login(String username, String password);

    CompletableFuture<AuthTokenResponse> refresh(String refreshToken);

    CompletableFuture<CurrentUserResponse> currentUser();

    CompletableFuture<Void> logout();

    void setAccessToken(String accessToken);
}
