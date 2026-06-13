package com.saulpos.server.security.web;

import com.saulpos.api.security.UserAccountCreateRequest;
import com.saulpos.api.security.UserAccountPasswordResetRequest;
import com.saulpos.api.security.UserAccountResponse;
import com.saulpos.server.security.service.SecurityManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final SecurityManagementService securityManagementService;

    @GetMapping
    public List<UserAccountResponse> list() {
        return securityManagementService.listUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountResponse create(@Valid @RequestBody UserAccountCreateRequest request) {
        return securityManagementService.createUser(request);
    }

    @PostMapping("/{id}/activate")
    public UserAccountResponse activate(@PathVariable("id") Long id) {
        return securityManagementService.setUserActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public UserAccountResponse deactivate(@PathVariable("id") Long id) {
        return securityManagementService.setUserActive(id, false);
    }

    @PostMapping("/{id}/password-reset")
    public UserAccountResponse resetPassword(@PathVariable("id") Long id,
                                             @Valid @RequestBody UserAccountPasswordResetRequest request) {
        return securityManagementService.resetUserPassword(id, request);
    }
}
