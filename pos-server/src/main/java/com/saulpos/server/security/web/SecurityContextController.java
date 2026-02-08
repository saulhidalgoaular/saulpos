package com.saulpos.server.security.web;

import com.saulpos.api.auth.CurrentUserResponse;
import com.saulpos.server.security.service.AuthContextService;
import com.saulpos.server.security.service.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityContextController {

    private final AuthContextService authContextService;

    @GetMapping("/me")
    public CurrentUserResponse me() {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        return new CurrentUserResponse(user.userId(), user.username(), user.roles());
    }
}
