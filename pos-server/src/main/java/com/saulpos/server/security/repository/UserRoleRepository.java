package com.saulpos.server.security.repository;

import com.saulpos.server.security.model.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    @Query("select ur.role.code from UserRoleEntity ur where ur.user.id = :userId")
    Set<String> findRoleCodesByUserId(@Param("userId") Long userId);
}
