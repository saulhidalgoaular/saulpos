package com.saulpos.server.identity.web;

import com.saulpos.api.identity.MerchantRequest;
import com.saulpos.api.identity.MerchantResponse;
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
@RequestMapping("/api/identity/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final IdentityService identityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantResponse create(@Valid @RequestBody MerchantRequest request) {
        return identityService.createMerchant(request);
    }

    @GetMapping
    public List<MerchantResponse> list() {
        return identityService.listMerchants();
    }

    @GetMapping("/{id}")
    public MerchantResponse get(@PathVariable("id") Long id) {
        return identityService.getMerchant(id);
    }

    @PutMapping("/{id}")
    public MerchantResponse update(@PathVariable("id") Long id, @Valid @RequestBody MerchantRequest request) {
        return identityService.updateMerchant(id, request);
    }

    @PostMapping("/{id}/activate")
    public MerchantResponse activate(@PathVariable("id") Long id) {
        return identityService.setMerchantActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public MerchantResponse deactivate(@PathVariable("id") Long id) {
        return identityService.setMerchantActive(id, false);
    }
}
