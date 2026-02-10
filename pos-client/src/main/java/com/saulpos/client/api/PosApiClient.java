package com.saulpos.client.api;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.CurrentUserResponse;
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
import com.saulpos.api.report.SalesReturnsReportResponse;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

    CompletableFuture<List<ProductResponse>> listProducts(Long merchantId, Boolean active, String query);

    CompletableFuture<ProductResponse> createProduct(ProductRequest request);

    CompletableFuture<ProductResponse> updateProduct(Long productId, ProductRequest request);

    CompletableFuture<PriceResolutionResponse> resolvePrice(Long storeLocationId, Long productId, Long customerId);

    CompletableFuture<SaleCartResponse> createCart(SaleCartCreateRequest request);

    CompletableFuture<SaleCartResponse> getCart(Long cartId);

    CompletableFuture<SaleCartResponse> addCartLine(Long cartId, SaleCartAddLineRequest request);

    CompletableFuture<SaleCartResponse> updateCartLine(Long cartId, Long lineId, SaleCartUpdateLineRequest request);

    CompletableFuture<SaleCartResponse> removeCartLine(Long cartId, Long lineId);

    CompletableFuture<SaleCartResponse> recalculateCart(Long cartId);

    CompletableFuture<SaleCheckoutResponse> checkout(SaleCheckoutRequest request);

    CompletableFuture<SaleReturnLookupResponse> lookupReturnByReceipt(String receiptNumber);

    CompletableFuture<SaleReturnResponse> submitReturn(SaleReturnSubmitRequest request);

    CompletableFuture<List<CustomerResponse>> listCustomers(Long merchantId, Boolean active);

    CompletableFuture<List<CustomerResponse>> lookupCustomers(Long merchantId,
                                                              String documentType,
                                                              String documentValue,
                                                              String email,
                                                              String phone);

    CompletableFuture<CustomerResponse> createCustomer(CustomerRequest request);

    CompletableFuture<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request);

    default CompletableFuture<SalesReturnsReportResponse> getSalesReturnsReport(Instant from,
                                                                                Instant to,
                                                                                Long storeLocationId,
                                                                                Long terminalDeviceId,
                                                                                Long cashierUserId,
                                                                                Long categoryId,
                                                                                Long taxGroupId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportSalesReturnsReportCsv(Instant from,
                                                                  Instant to,
                                                                  Long storeLocationId,
                                                                  Long terminalDeviceId,
                                                                  Long cashierUserId,
                                                                  Long categoryId,
                                                                  Long taxGroupId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<InventoryStockOnHandReportResponse> getInventoryStockOnHandReport(Long storeLocationId,
                                                                                                  Long categoryId,
                                                                                                  Long supplierId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportInventoryStockOnHandReportCsv(Long storeLocationId,
                                                                           Long categoryId,
                                                                           Long supplierId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<InventoryLowStockReportResponse> getInventoryLowStockReport(Long storeLocationId,
                                                                                           Long categoryId,
                                                                                           Long supplierId,
                                                                                           BigDecimal minimumQuantity) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportInventoryLowStockReportCsv(Long storeLocationId,
                                                                        Long categoryId,
                                                                        Long supplierId,
                                                                        BigDecimal minimumQuantity) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<InventoryMovementReportResponse> getInventoryMovementReport(Instant from,
                                                                                           Instant to,
                                                                                           Long storeLocationId,
                                                                                           Long categoryId,
                                                                                           Long supplierId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportInventoryMovementReportCsv(Instant from,
                                                                       Instant to,
                                                                       Long storeLocationId,
                                                                       Long categoryId,
                                                                       Long supplierId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<CashShiftReportResponse> getCashShiftReport(Instant from,
                                                                           Instant to,
                                                                           Long storeLocationId,
                                                                           Long terminalDeviceId,
                                                                           Long cashierUserId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportCashShiftReportCsv(Instant from,
                                                                Instant to,
                                                                Long storeLocationId,
                                                                Long terminalDeviceId,
                                                                Long cashierUserId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<EndOfDayCashReportResponse> getEndOfDayCashReport(Instant from,
                                                                                 Instant to,
                                                                                 Long storeLocationId,
                                                                                 Long terminalDeviceId,
                                                                                 Long cashierUserId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportEndOfDayCashReportCsv(Instant from,
                                                                   Instant to,
                                                                   Long storeLocationId,
                                                                   Long terminalDeviceId,
                                                                   Long cashierUserId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<ExceptionReportResponse> getExceptionReport(Instant from,
                                                                          Instant to,
                                                                          Long storeLocationId,
                                                                          Long terminalDeviceId,
                                                                          Long cashierUserId,
                                                                          String reasonCode,
                                                                          ExceptionReportEventType eventType) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    default CompletableFuture<String> exportExceptionReportCsv(Instant from,
                                                               Instant to,
                                                               Long storeLocationId,
                                                               Long terminalDeviceId,
                                                               Long cashierUserId,
                                                               String reasonCode,
                                                               ExceptionReportEventType eventType) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    void setAccessToken(String accessToken);
}
