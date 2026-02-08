package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {

    Optional<UserAccountEntity> findByUsernameIgnoreCase(String username);
}
