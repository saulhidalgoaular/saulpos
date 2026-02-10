package com.saulpos.server.system.web;

import com.saulpos.api.system.OfflinePolicyResponse;
import com.saulpos.server.system.service.OfflinePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemPolicyController {

    private final OfflinePolicyService offlinePolicyService;

    @GetMapping("/offline-policy")
    public OfflinePolicyResponse offlinePolicy() {
        return offlinePolicyService.currentPolicy();
    }
}
