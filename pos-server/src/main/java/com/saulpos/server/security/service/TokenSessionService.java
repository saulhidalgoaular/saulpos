package com.saulpos.server.security.service;

import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.security.config.SecurityProperties;
import com.saulpos.server.security.model.AuthSessionEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.AuthSessionRepository;
import com.saulpos.server.security.repository.UserRoleRepository;
import com.saulpos.server.security.support.TokenHashingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private final AuthSessionRepository authSessionRepository;
    private final UserRoleRepository userRoleRepository;
    private final TokenHashingService tokenHashingService;
    private final SecurityProperties securityProperties;
    private final Clock clock;

    @Transactional(readOnly = true)
    public AccessTokenValidationResult validateAccessToken(String accessToken) {
        String accessTokenHash = tokenHashingService.hash(accessToken);
        AuthSessionEntity session = authSessionRepository.findByAccessTokenHashAndRevokedAtIsNull(accessTokenHash)
                .orElse(null);
        if (session == null) {
            return AccessTokenValidationResult.failed(AccessTokenValidationStatus.INVALID);
        }

        Instant now = Instant.now(clock);
        if (session.getAccessExpiresAt().isBefore(now)) {
            return AccessTokenValidationResult.failed(AccessTokenValidationStatus.EXPIRED);
        }

        UserAccountEntity user = session.getUser();
        if (user == null || !user.isActive()) {
            return AccessTokenValidationResult.failed(AccessTokenValidationStatus.INACTIVE);
        }

        Set<String> roleCodes = userRoleRepository.findRoleCodesByUserId(user.getId());
        Set<String> permissionCodes = userRoleRepository.findPermissionCodesByUserId(user.getId());
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                session.getId(),
                user.getUsername(),
                roleCodes,
                permissionCodes);

        return AccessTokenValidationResult.authenticated(authenticatedUser);
    }

    @Transactional
    public SessionTokenBundle createSession(UserAccountEntity user) {
        Instant now = Instant.now(clock);
        String accessToken = tokenHashingService.generateOpaqueToken();
        String refreshToken = tokenHashingService.generateOpaqueToken();
        Instant accessExpiresAt = now.plus(Duration.ofMinutes(securityProperties.getAccessTokenTtlMinutes()));
        Instant refreshExpiresAt = now.plus(Duration.ofMinutes(securityProperties.getRefreshTokenTtlMinutes()));

        AuthSessionEntity session = new AuthSessionEntity();
        session.setUser(user);
        session.setAccessTokenHash(tokenHashingService.hash(accessToken));
        session.setRefreshTokenHash(tokenHashingService.hash(refreshToken));
        session.setAccessExpiresAt(accessExpiresAt);
        session.setRefreshExpiresAt(refreshExpiresAt);

        AuthSessionEntity savedSession = authSessionRepository.save(session);

        return new SessionTokenBundle(
                user.getId(),
                savedSession.getId(),
                accessToken,
                refreshToken,
                accessExpiresAt,
                refreshExpiresAt);
    }

    @Transactional
    public SessionTokenBundle rotateSession(String refreshToken) {
        String refreshTokenHash = tokenHashingService.hash(refreshToken);
        AuthSessionEntity session = authSessionRepository.findByRefreshTokenHashAndRevokedAtIsNull(refreshTokenHash)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_TOKEN_INVALID));

        Instant now = Instant.now(clock);
        if (session.getRefreshExpiresAt().isBefore(now)) {
            throw new BaseException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        UserAccountEntity user = session.getUser();
        if (user == null || !user.isActive()) {
            throw new BaseException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        String newAccessToken = tokenHashingService.generateOpaqueToken();
        String newRefreshToken = tokenHashingService.generateOpaqueToken();
        Instant newAccessExpiresAt = now.plus(Duration.ofMinutes(securityProperties.getAccessTokenTtlMinutes()));
        Instant newRefreshExpiresAt = now.plus(Duration.ofMinutes(securityProperties.getRefreshTokenTtlMinutes()));

        session.setAccessTokenHash(tokenHashingService.hash(newAccessToken));
        session.setRefreshTokenHash(tokenHashingService.hash(newRefreshToken));
        session.setAccessExpiresAt(newAccessExpiresAt);
        session.setRefreshExpiresAt(newRefreshExpiresAt);

        AuthSessionEntity savedSession = authSessionRepository.save(session);
        return new SessionTokenBundle(
                user.getId(),
                savedSession.getId(),
                newAccessToken,
                newRefreshToken,
                newAccessExpiresAt,
                newRefreshExpiresAt);
    }

    @Transactional
    public void revokeSession(Long sessionId) {
        authSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now(clock));
                authSessionRepository.save(session);
            }
        });
    }

    @Transactional(readOnly = true)
    public Set<String> findRoleCodesByUserId(Long userId) {
        return userRoleRepository.findRoleCodesByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Set<String> findPermissionCodesByUserId(Long userId) {
        return userRoleRepository.findPermissionCodesByUserId(userId);
    }
}
