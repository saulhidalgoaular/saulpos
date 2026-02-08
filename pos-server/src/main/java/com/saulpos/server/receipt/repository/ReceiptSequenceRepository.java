package com.saulpos.server.receipt.repository;

import com.saulpos.server.receipt.model.ReceiptSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReceiptSequenceRepository extends JpaRepository<ReceiptSequenceEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select seq
            from ReceiptSequenceEntity seq
            where seq.seriesId = :seriesId
            """)
    Optional<ReceiptSequenceEntity> findBySeriesIdForUpdate(@Param("seriesId") Long seriesId);
}
