package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, Long> {

    Optional<AuthSessionEntity> findByAccessTokenHashAndRevokedAtIsNull(String accessTokenHash);

    Optional<AuthSessionEntity> findByRefreshTokenHashAndRevokedAtIsNull(String refreshTokenHash);
}
