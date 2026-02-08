package com.saulpos.server.security.web;

import com.saulpos.api.security.CurrentUserPermissionsResponse;
import com.saulpos.api.security.PermissionResponse;
import com.saulpos.server.security.service.SecurityManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final SecurityManagementService securityManagementService;

    @GetMapping("/current")
    public CurrentUserPermissionsResponse current() {
        return securityManagementService.currentUserPermissions();
    }

    @GetMapping("/catalog")
    public List<PermissionResponse> catalog() {
        return securityManagementService.listPermissionCatalog();
    }
}
