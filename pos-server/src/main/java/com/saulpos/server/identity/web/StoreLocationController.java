package com.saulpos.server.identity.web;

import com.saulpos.api.identity.StoreLocationRequest;
import com.saulpos.api.identity.StoreLocationResponse;
import com.saulpos.server.identity.service.IdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/identity/stores")
@RequiredArgsConstructor
public class StoreLocationController {

    private final IdentityService identityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreLocationResponse create(@Valid @RequestBody StoreLocationRequest request) {
        return identityService.createStoreLocation(request);
    }

    @GetMapping
    public List<StoreLocationResponse> list() {
        return identityService.listStoreLocations();
    }

    @GetMapping("/{id}")
    public StoreLocationResponse get(@PathVariable("id") Long id) {
        return identityService.getStoreLocation(id);
    }

    @PutMapping("/{id}")
    public StoreLocationResponse update(@PathVariable("id") Long id, @Valid @RequestBody StoreLocationRequest request) {
        return identityService.updateStoreLocation(id, request);
    }

    @PostMapping("/{id}/activate")
    public StoreLocationResponse activate(@PathVariable("id") Long id) {
        return identityService.setStoreLocationActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public StoreLocationResponse deactivate(@PathVariable("id") Long id) {
        return identityService.setStoreLocationActive(id, false);
    }
}
