package com.saulpos.client.api;

import java.util.concurrent.CompletableFuture;

public interface PosApiClient {

    CompletableFuture<Boolean> ping();

    CompletableFuture<Void> login(String username, String password);

    CompletableFuture<Void> logout();

    void setAccessToken(String accessToken);
}
