package com.saulpos.server.storecredit.repository;

import com.saulpos.server.storecredit.model.StoreCreditAccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreCreditAccountRepository extends JpaRepository<StoreCreditAccountEntity, Long> {

    @EntityGraph(attributePaths = {"transactions"})
    @Query("""
            select account
            from StoreCreditAccountEntity account
            where account.id = :accountId
              and account.merchant.id = :merchantId
            """)
    Optional<StoreCreditAccountEntity> findByIdAndMerchantId(
            @Param("accountId") Long accountId,
            @Param("merchantId") Long merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select account
            from StoreCreditAccountEntity account
            where account.id = :accountId
              and account.merchant.id = :merchantId
            """)
    Optional<StoreCreditAccountEntity> findByIdAndMerchantIdForUpdate(
            @Param("accountId") Long accountId,
            @Param("merchantId") Long merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select account
            from StoreCreditAccountEntity account
            where account.merchant.id = :merchantId
              and account.customer.id = :customerId
            """)
    Optional<StoreCreditAccountEntity> findByMerchantIdAndCustomerIdForUpdate(
            @Param("merchantId") Long merchantId,
            @Param("customerId") Long customerId);
}
