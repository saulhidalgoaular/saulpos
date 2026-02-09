package com.saulpos.server.inventory.web;

import com.saulpos.api.inventory.InventoryMovementCreateRequest;
import com.saulpos.api.inventory.InventoryMovementResponse;
import com.saulpos.api.inventory.InventoryStockBalanceResponse;
import com.saulpos.server.inventory.service.InventoryLedgerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
