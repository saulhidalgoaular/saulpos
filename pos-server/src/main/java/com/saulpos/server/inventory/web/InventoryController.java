package com.saulpos.server.inventory.web;

import com.saulpos.api.inventory.InventoryMovementCreateRequest;
import com.saulpos.api.inventory.InventoryMovementResponse;
import com.saulpos.api.inventory.InventoryStockBalanceResponse;
import com.saulpos.api.inventory.StockAdjustmentApproveRequest;
import com.saulpos.api.inventory.StockAdjustmentCreateRequest;
import com.saulpos.api.inventory.StockAdjustmentPostRequest;
import com.saulpos.api.inventory.StockAdjustmentResponse;
import com.saulpos.api.inventory.StockTransferCreateRequest;
import com.saulpos.api.inventory.StockTransferReceiveRequest;
import com.saulpos.api.inventory.StockTransferResponse;
import com.saulpos.api.inventory.StockTransferShipRequest;
import com.saulpos.api.inventory.StocktakeCreateRequest;
import com.saulpos.api.inventory.StocktakeFinalizeRequest;
import com.saulpos.api.inventory.StocktakeSessionResponse;
import com.saulpos.api.inventory.StocktakeVarianceReportResponse;
import com.saulpos.server.inventory.service.InventoryLedgerService;
import com.saulpos.server.inventory.service.StockAdjustmentService;
import com.saulpos.server.inventory.service.StockTransferService;
import com.saulpos.server.inventory.service.StocktakeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryLedgerService inventoryLedgerService;
    private final StockAdjustmentService stockAdjustmentService;
    private final StocktakeService stocktakeService;
    private final StockTransferService stockTransferService;

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public StockAdjustmentResponse createAdjustment(@Valid @RequestBody StockAdjustmentCreateRequest request) {
        return stockAdjustmentService.createAdjustment(request);
    }

    @PostMapping("/adjustments/{adjustmentId}/approve")
    public StockAdjustmentResponse approveAdjustment(@PathVariable("adjustmentId") Long adjustmentId,
                                                     @Valid @RequestBody(required = false) StockAdjustmentApproveRequest request) {
        return stockAdjustmentService.approveAdjustment(adjustmentId, request);
    }

    @PostMapping("/adjustments/{adjustmentId}/post")
    public StockAdjustmentResponse postAdjustment(@PathVariable("adjustmentId") Long adjustmentId,
                                                  @Valid @RequestBody(required = false) StockAdjustmentPostRequest request) {
        return stockAdjustmentService.postAdjustment(adjustmentId, request);
    }

    @PostMapping("/stocktakes")
    @ResponseStatus(HttpStatus.CREATED)
    public StocktakeSessionResponse createStocktake(@Valid @RequestBody StocktakeCreateRequest request) {
        return stocktakeService.createStocktake(request);
    }

    @PostMapping("/stocktakes/{stocktakeId}/start")
    public StocktakeSessionResponse startStocktake(@PathVariable("stocktakeId") Long stocktakeId) {
        return stocktakeService.startStocktake(stocktakeId);
    }

    @PostMapping("/stocktakes/{stocktakeId}/finalize")
    public StocktakeSessionResponse finalizeStocktake(@PathVariable("stocktakeId") Long stocktakeId,
                                                      @Valid @RequestBody StocktakeFinalizeRequest request) {
        return stocktakeService.finalizeStocktake(stocktakeId, request);
    }

    @GetMapping("/stocktakes/{stocktakeId}/variance")
    public StocktakeVarianceReportResponse getStocktakeVarianceReport(@PathVariable("stocktakeId") Long stocktakeId) {
        return stocktakeService.getVarianceReport(stocktakeId);
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransferResponse createTransfer(@Valid @RequestBody StockTransferCreateRequest request) {
        return stockTransferService.createTransfer(request);
    }

    @GetMapping("/transfers/{transferId}")
    public StockTransferResponse getTransfer(@PathVariable("transferId") Long transferId) {
        return stockTransferService.getTransfer(transferId);
    }

    @PostMapping("/transfers/{transferId}/ship")
    public StockTransferResponse shipTransfer(@PathVariable("transferId") Long transferId,
                                              @Valid @RequestBody StockTransferShipRequest request) {
        return stockTransferService.shipTransfer(transferId, request);
    }

    @PostMapping("/transfers/{transferId}/receive")
    public StockTransferResponse receiveTransfer(@PathVariable("transferId") Long transferId,
                                                 @Valid @RequestBody StockTransferReceiveRequest request) {
        return stockTransferService.receiveTransfer(transferId, request);
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse createMovement(@Valid @RequestBody InventoryMovementCreateRequest request) {
        return inventoryLedgerService.createMovement(request);
    }

    @GetMapping("/movements")
    public List<InventoryMovementResponse> listMovements(
            @RequestParam("storeLocationId")
            @NotNull(message = "storeLocationId is required") Long storeLocationId,
            @RequestParam(value = "productId", required = false) Long productId) {
        return inventoryLedgerService.listMovements(storeLocationId, productId);
    }

    @GetMapping("/balances")
    public List<InventoryStockBalanceResponse> listBalances(
            @RequestParam("storeLocationId")
            @NotNull(message = "storeLocationId is required") Long storeLocationId,
            @RequestParam(value = "productId", required = false) Long productId) {
        return inventoryLedgerService.listStockBalances(storeLocationId, productId);
    }
}
