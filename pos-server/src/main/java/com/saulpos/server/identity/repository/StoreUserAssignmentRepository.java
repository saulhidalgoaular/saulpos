package com.saulpos.server.identity.repository;

import com.saulpos.server.identity.model.StoreUserAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreUserAssignmentRepository extends JpaRepository<StoreUserAssignmentEntity, Long> {
    Optional<StoreUserAssignmentEntity> findByUserIdAndStoreLocationIdAndRoleId(Long userId, Long storeLocationId, Long roleId);
}
