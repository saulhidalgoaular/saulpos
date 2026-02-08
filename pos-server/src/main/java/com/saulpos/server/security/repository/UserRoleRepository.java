package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    @Query("select ur.role.code from UserRoleEntity ur where ur.user.id = :userId")
    Set<String> findRoleCodesByUserId(@Param("userId") Long userId);

    @Query("""
            select distinct rp.permission.code
            from UserRoleEntity ur
            join RolePermissionEntity rp on rp.role = ur.role
            where ur.user.id = :userId
            """)
    Set<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
