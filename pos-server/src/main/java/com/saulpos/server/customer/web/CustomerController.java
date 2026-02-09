package com.saulpos.server.customer.web;

import com.saulpos.api.customer.CustomerRequest;
import com.saulpos.api.customer.CustomerResponse;
import com.saulpos.api.customer.CustomerGroupAssignmentRequest;
import com.saulpos.api.customer.CustomerGroupRequest;
import com.saulpos.api.customer.CustomerGroupResponse;
import com.saulpos.api.customer.CustomerReturnsHistoryResponse;
import com.saulpos.api.customer.CustomerSalesHistoryResponse;
import com.saulpos.server.customer.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.Instant;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerGroupResponse createGroup(@Valid @RequestBody CustomerGroupRequest request) {
        return customerService.createCustomerGroup(request);
    }

    @GetMapping
    public List<CustomerResponse> list(@RequestParam(value = "merchantId", required = false) Long merchantId,
                                       @RequestParam(value = "active", required = false) Boolean active) {
        return customerService.listCustomers(merchantId, active);
    }

    @GetMapping("/groups")
    public List<CustomerGroupResponse> listGroups(
            @RequestParam("merchantId") @NotNull(message = "merchantId is required") Long merchantId,
            @RequestParam(value = "active", required = false) Boolean active) {
        return customerService.listCustomerGroups(merchantId, active);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable("id") Long id) {
        return customerService.getCustomer(id);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable("id") Long id,
                                   @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @PutMapping("/{id}/groups")
    public CustomerResponse assignGroups(@PathVariable("id") Long id,
                                         @Valid @RequestBody CustomerGroupAssignmentRequest request) {
        return customerService.assignCustomerGroups(id, request);
    }

    @PostMapping("/{id}/activate")
    public CustomerResponse activate(@PathVariable("id") Long id) {
        return customerService.setCustomerActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public CustomerResponse deactivate(@PathVariable("id") Long id) {
        return customerService.setCustomerActive(id, false);
    }

    @GetMapping("/{id}/groups")
    public List<CustomerGroupResponse> listCustomerGroups(@PathVariable("id") Long id) {
        return customerService.listCustomerGroupsForCustomer(id);
    }

    @GetMapping("/{id}/sales")
    public CustomerSalesHistoryResponse listCustomerSalesHistory(
            @PathVariable("id") Long id,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "page", defaultValue = "0")
            @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(value = "size", defaultValue = "20")
            @Min(value = 1, message = "size must be >= 1")
            @Max(value = 100, message = "size must be <= 100") int size) {
        return customerService.listCustomerSalesHistory(id, from, to, page, size);
    }

    @GetMapping("/{id}/returns")
    public CustomerReturnsHistoryResponse listCustomerReturnsHistory(
            @PathVariable("id") Long id,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "page", defaultValue = "0")
            @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(value = "size", defaultValue = "20")
            @Min(value = 1, message = "size must be >= 1")
            @Max(value = 100, message = "size must be <= 100") int size) {
        return customerService.listCustomerReturnsHistory(id, from, to, page, size);
    }

    @GetMapping("/lookup")
    public List<CustomerResponse> lookup(
            @RequestParam("merchantId") @NotNull(message = "merchantId is required") Long merchantId,
            @RequestParam(value = "documentType", required = false)
            @Size(max = 40, message = "documentType must be at most 40 characters") String documentType,
            @RequestParam(value = "documentValue", required = false)
            @Size(max = 80, message = "documentValue must be at most 80 characters") String documentValue,
            @RequestParam(value = "email", required = false)
            @Size(max = 120, message = "email must be at most 120 characters") String email,
            @RequestParam(value = "phone", required = false)
            @Size(max = 120, message = "phone must be at most 120 characters") String phone) {
        return customerService.lookupCustomers(merchantId, documentType, documentValue, email, phone);
    }
}
