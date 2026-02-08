package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {
}
