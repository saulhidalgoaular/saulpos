package com.saulpos.server.identity.web;

import com.saulpos.api.identity.StoreUserAssignmentRequest;
import com.saulpos.api.identity.StoreUserAssignmentResponse;
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
@RequestMapping("/api/identity/store-user-assignments")
@RequiredArgsConstructor
public class StoreUserAssignmentController {

    private final IdentityService identityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreUserAssignmentResponse create(@Valid @RequestBody StoreUserAssignmentRequest request) {
        return identityService.createStoreUserAssignment(request);
    }

    @GetMapping
    public List<StoreUserAssignmentResponse> list() {
        return identityService.listStoreUserAssignments();
    }

    @GetMapping("/{id}")
    public StoreUserAssignmentResponse get(@PathVariable("id") Long id) {
        return identityService.getStoreUserAssignment(id);
    }

    @PutMapping("/{id}")
    public StoreUserAssignmentResponse update(@PathVariable("id") Long id,
                                              @Valid @RequestBody StoreUserAssignmentRequest request) {
        return identityService.updateStoreUserAssignment(id, request);
    }

    @PostMapping("/{id}/activate")
    public StoreUserAssignmentResponse activate(@PathVariable("id") Long id) {
        return identityService.setStoreUserAssignmentActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public StoreUserAssignmentResponse deactivate(@PathVariable("id") Long id) {
        return identityService.setStoreUserAssignmentActive(id, false);
    }
}
