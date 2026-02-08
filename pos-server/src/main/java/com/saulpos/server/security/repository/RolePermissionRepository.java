package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    @Query("select rp.permission.code from RolePermissionEntity rp where rp.role.id = :roleId")
    Set<String> findPermissionCodesByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("delete from RolePermissionEntity rp where rp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
