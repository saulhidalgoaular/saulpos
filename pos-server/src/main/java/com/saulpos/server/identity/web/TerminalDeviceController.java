package com.saulpos.server.identity.web;

import com.saulpos.api.identity.TerminalDeviceRequest;
import com.saulpos.api.identity.TerminalDeviceResponse;
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
@RequestMapping("/api/identity/terminals")
@RequiredArgsConstructor
public class TerminalDeviceController {

    private final IdentityService identityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerminalDeviceResponse create(@Valid @RequestBody TerminalDeviceRequest request) {
        return identityService.createTerminalDevice(request);
    }

    @GetMapping
    public List<TerminalDeviceResponse> list() {
        return identityService.listTerminalDevices();
    }

    @GetMapping("/{id}")
    public TerminalDeviceResponse get(@PathVariable("id") Long id) {
        return identityService.getTerminalDevice(id);
    }

    @PutMapping("/{id}")
    public TerminalDeviceResponse update(@PathVariable("id") Long id, @Valid @RequestBody TerminalDeviceRequest request) {
        return identityService.updateTerminalDevice(id, request);
    }

    @PostMapping("/{id}/activate")
    public TerminalDeviceResponse activate(@PathVariable("id") Long id) {
        return identityService.setTerminalDeviceActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public TerminalDeviceResponse deactivate(@PathVariable("id") Long id) {
        return identityService.setTerminalDeviceActive(id, false);
    }
}
