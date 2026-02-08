package com.saulpos.server.security.web;

import com.saulpos.api.security.RolePermissionsUpdateRequest;
import com.saulpos.api.security.RoleRequest;
import com.saulpos.api.security.RoleResponse;
import com.saulpos.server.security.service.SecurityManagementService;
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
@RequestMapping("/api/security/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SecurityManagementService securityManagementService;

    @GetMapping
    public List<RoleResponse> list() {
        return securityManagementService.listRoles();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody RoleRequest request) {
        return securityManagementService.createRole(request);
    }

    @PutMapping("/{id}/permissions")
    public RoleResponse updatePermissions(@PathVariable("id") Long id,
                                          @Valid @RequestBody RolePermissionsUpdateRequest request) {
        return securityManagementService.updateRolePermissions(id, request);
    }
}
