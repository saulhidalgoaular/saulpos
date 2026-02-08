package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByCode(String code);

    List<PermissionEntity> findAllByOrderByCodeAsc();

    List<PermissionEntity> findByCodeIn(Collection<String> codes);
}
