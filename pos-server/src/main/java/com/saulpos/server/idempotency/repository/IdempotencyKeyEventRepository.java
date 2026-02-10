package com.saulpos.server.idempotency.repository;

import com.saulpos.server.idempotency.model.IdempotencyKeyEventEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyKeyEventRepository extends JpaRepository<IdempotencyKeyEventEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from IdempotencyKeyEventEntity event
            where event.endpointKey = :endpointKey
              and event.idempotencyKey = :idempotencyKey
            """)
    Optional<IdempotencyKeyEventEntity> findByEndpointKeyAndIdempotencyKeyForUpdate(
            @Param("endpointKey") String endpointKey,
            @Param("idempotencyKey") String idempotencyKey);
}
