package com.saulpos.client.api;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
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

    CompletableFuture<ProductLookupResponse> lookupProductByBarcode(Long merchantId, String barcode);

    CompletableFuture<ProductSearchResponse> searchProducts(Long merchantId, String query, Boolean active, int page, int size);

    CompletableFuture<SaleCartResponse> createCart(SaleCartCreateRequest request);

    CompletableFuture<SaleCartResponse> getCart(Long cartId);

    CompletableFuture<SaleCartResponse> addCartLine(Long cartId, SaleCartAddLineRequest request);

    CompletableFuture<SaleCartResponse> updateCartLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request);

    CompletableFuture<SaleCartResponse> removeCartLine(Long cartId, Long lineId);

    CompletableFuture<SaleCartResponse> recalculateCart(Long cartId);

    CompletableFuture<SaleCheckoutResponse> checkout(SaleCheckoutRequest request);

    void setAccessToken(String accessToken);
}
