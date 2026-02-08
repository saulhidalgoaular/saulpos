package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.AuthAuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEventEntity, Long> {
}
