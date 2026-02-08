package com.saulpos.server.catalog.web;

import com.saulpos.api.catalog.OpenPriceEntryValidationRequest;
import com.saulpos.api.catalog.OpenPriceEntryValidationResponse;
import com.saulpos.api.catalog.ProductLookupResponse;
import com.saulpos.api.catalog.ProductRequest;
import com.saulpos.api.catalog.ProductResponse;
import com.saulpos.api.catalog.ProductSearchResponse;
import com.saulpos.server.catalog.service.CatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final CatalogService catalogService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return catalogService.createProduct(request);
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(value = "merchantId", required = false) Long merchantId,
                                      @RequestParam(value = "active", required = false) Boolean active,
                                      @RequestParam(value = "q", required = false) String query) {
        return catalogService.listProducts(merchantId, active, query);
    }

    @GetMapping("/lookup")
    public ProductLookupResponse lookup(@RequestParam("merchantId") @NotNull(message = "merchantId is required") Long merchantId,
                                        @RequestParam("barcode")
                                        @NotBlank(message = "barcode is required")
                                        @Size(max = 64, message = "barcode must be at most 64 characters") String barcode) {
        return catalogService.lookupByBarcode(merchantId, barcode);
    }

    @GetMapping("/search")
    public ProductSearchResponse search(@RequestParam("merchantId") @NotNull(message = "merchantId is required") Long merchantId,
                                        @RequestParam("q")
                                        @NotBlank(message = "q is required")
                                        @Size(max = 160, message = "q must be at most 160 characters") String query,
                                        @RequestParam(value = "active", required = false) Boolean active,
                                        @RequestParam(value = "page", defaultValue = "0")
                                        @Min(value = 0, message = "page must be >= 0") int page,
                                        @RequestParam(value = "size", defaultValue = "20")
                                        @Min(value = 1, message = "size must be >= 1")
                                        @Max(value = 100, message = "size must be <= 100") int size) {
        return catalogService.searchProducts(merchantId, active, query, page, size);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable("id") Long id) {
        return catalogService.getProduct(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest request) {
        return catalogService.updateProduct(id, request);
    }

    @PostMapping("/{id}/activate")
    public ProductResponse activate(@PathVariable("id") Long id) {
        return catalogService.setProductActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public ProductResponse deactivate(@PathVariable("id") Long id) {
        return catalogService.setProductActive(id, false);
    }

    @PostMapping("/{id}/open-price/validate")
    public OpenPriceEntryValidationResponse validateOpenPriceEntry(@PathVariable("id") Long id,
                                                                   @Valid @RequestBody OpenPriceEntryValidationRequest request) {
        return catalogService.validateOpenPriceEntry(id, request);
    }
}
