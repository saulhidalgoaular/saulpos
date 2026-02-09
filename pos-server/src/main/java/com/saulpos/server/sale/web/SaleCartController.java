package com.saulpos.server.sale.web;

import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCancelRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartParkRequest;
import com.saulpos.api.sale.SaleCartRecalculateRequest;
import com.saulpos.api.sale.SaleCartResumeRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCartUpdateLineRequest;
import com.saulpos.api.sale.ParkedSaleCartSummaryResponse;
import com.saulpos.server.sale.service.SaleCartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales/carts")
@RequiredArgsConstructor
@Validated
public class SaleCartController {

    private final SaleCartService saleCartService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleCartResponse create(@Valid @RequestBody SaleCartCreateRequest request) {
        return saleCartService.createCart(request);
    }

    @GetMapping("/{id}")
    public SaleCartResponse get(@PathVariable("id") Long id) {
        return saleCartService.getCart(id);
    }

    @PostMapping("/{id}/lines")
    public SaleCartResponse addLine(@PathVariable("id") Long id,
                                    @Valid @RequestBody SaleCartAddLineRequest request) {
        return saleCartService.addLine(id, request);
    }

    @PutMapping("/{id}/lines/{lineId}")
    public SaleCartResponse updateLine(@PathVariable("id") Long id,
                                       @PathVariable("lineId") Long lineId,
                                       @Valid @RequestBody SaleCartUpdateLineRequest request) {
        return saleCartService.updateLine(id, lineId, request);
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    public SaleCartResponse removeLine(@PathVariable("id") Long id,
                                       @PathVariable("lineId") Long lineId) {
        return saleCartService.removeLine(id, lineId);
    }

    @PostMapping("/{id}/recalculate")
    public SaleCartResponse recalculate(@PathVariable("id") Long id,
                                        @RequestBody(required = false) SaleCartRecalculateRequest request) {
        return saleCartService.recalculate(id, request);
    }

    @PostMapping("/{id}/park")
    public SaleCartResponse park(@PathVariable("id") Long id,
                                 @Valid @RequestBody SaleCartParkRequest request) {
        return saleCartService.parkCart(id, request);
    }

    @PostMapping("/{id}/resume")
    public SaleCartResponse resume(@PathVariable("id") Long id,
                                   @Valid @RequestBody SaleCartResumeRequest request) {
        return saleCartService.resumeCart(id, request);
    }

    @PostMapping("/{id}/cancel")
    public SaleCartResponse cancel(@PathVariable("id") Long id,
                                   @Valid @RequestBody SaleCartCancelRequest request) {
        return saleCartService.cancelCart(id, request);
    }

    @GetMapping("/parked")
    public List<ParkedSaleCartSummaryResponse> listParked(
            @RequestParam("storeLocationId") @NotNull(message = "storeLocationId is required") Long storeLocationId,
            @RequestParam(value = "terminalDeviceId", required = false) Long terminalDeviceId) {
        return saleCartService.listParkedCarts(storeLocationId, terminalDeviceId);
    }
}
