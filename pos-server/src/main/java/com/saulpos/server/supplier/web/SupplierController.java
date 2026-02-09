package com.saulpos.server.supplier.web;

import com.saulpos.api.supplier.SupplierRequest;
import com.saulpos.api.supplier.SupplierResponse;
import com.saulpos.server.supplier.service.SupplierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Validated
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.createSupplier(request);
    }

    @GetMapping
    public List<SupplierResponse> list(
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "q", required = false)
            @Size(max = 80, message = "q must be at most 80 characters") String query) {
        return supplierService.listSuppliers(merchantId, active, query);
    }

    @GetMapping("/{id}")
    public SupplierResponse get(@PathVariable("id") Long id) {
        return supplierService.getSupplier(id);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable("id") Long id,
                                   @Valid @RequestBody SupplierRequest request) {
        return supplierService.updateSupplier(id, request);
    }

    @PostMapping("/{id}/activate")
    public SupplierResponse activate(@PathVariable("id") Long id) {
        return supplierService.setSupplierActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public SupplierResponse deactivate(@PathVariable("id") Long id) {
        return supplierService.setSupplierActive(id, false);
    }
}
