package com.saulpos.server.security.service;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.api.auth.LoginRequest;
import com.saulpos.api.auth.RefreshTokenRequest;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.security.config.SecurityProperties;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final TokenSessionService tokenSessionService;
    private final AuthAuditService authAuditService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final Clock clock;

    @Transactional
    public AuthTokenResponse login(LoginRequest loginRequest) {
        String username = loginRequest.username().trim();
        UserAccountEntity user = userAccountRepository.findByUsernameIgnoreCase(username).orElse(null);

        if (user == null) {
            authAuditService.loginFailure(username, null, "INVALID_CREDENTIALS");
            throw new BaseException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        Instant now = Instant.now(clock);
        if (!user.isActive()) {
            authAuditService.loginFailure(user.getUsername(), user, "USER_DISABLED");
            throw new BaseException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            authAuditService.loginFailure(user.getUsername(), user, "ACCOUNT_LOCKED");
            throw new BaseException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            return handleInvalidPassword(user, now);
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);

        SessionTokenBundle tokens = tokenSessionService.createSession(user);
        Set<String> roleCodes = tokenSessionService.findRoleCodesByUserId(user.getId());
        authAuditService.loginSuccess(user);

        return new AuthTokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.accessTokenExpiresAt(),
                tokens.refreshTokenExpiresAt(),
                roleCodes);
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest refreshTokenRequest) {
        SessionTokenBundle tokens = tokenSessionService.rotateSession(refreshTokenRequest.refreshToken());
        Set<String> roleCodes = tokenSessionService.findRoleCodesByUserId(tokens.userId());

        return new AuthTokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.accessTokenExpiresAt(),
                tokens.refreshTokenExpiresAt(),
                roleCodes);
    }

    @Transactional
    public void logout(AuthenticatedUser authenticatedUser) {
        UserAccountEntity user = userAccountRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_TOKEN_INVALID));

        tokenSessionService.revokeSession(authenticatedUser.sessionId());
        authAuditService.logout(user);
    }

    private AuthTokenResponse handleInvalidPassword(UserAccountEntity user, Instant now) {
        int failedAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(failedAttempts);

        if (failedAttempts >= securityProperties.getMaxFailedAttempts()) {
            user.setLockedUntil(now.plus(Duration.ofMinutes(securityProperties.getLockDurationMinutes())));
            userAccountRepository.save(user);
            authAuditService.loginFailure(user.getUsername(), user, "MAX_ATTEMPTS_REACHED");
            throw new BaseException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        userAccountRepository.save(user);
        authAuditService.loginFailure(user.getUsername(), user, "INVALID_PASSWORD");
        throw new BaseException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }
}
