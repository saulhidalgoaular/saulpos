package com.saulpos.server.giftcard.repository;

import com.saulpos.server.giftcard.model.GiftCardEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCardEntity, Long> {

    @EntityGraph(attributePaths = {"transactions"})
    @Query("""
            select giftCard
            from GiftCardEntity giftCard
            where giftCard.merchant.id = :merchantId
              and giftCard.cardNumberNormalized = :cardNumberNormalized
            """)
    Optional<GiftCardEntity> findByMerchantIdAndCardNumberNormalized(
            @Param("merchantId") Long merchantId,
            @Param("cardNumberNormalized") String cardNumberNormalized);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select giftCard
            from GiftCardEntity giftCard
            where giftCard.merchant.id = :merchantId
              and giftCard.cardNumberNormalized = :cardNumberNormalized
            """)
    Optional<GiftCardEntity> findByMerchantIdAndCardNumberNormalizedForUpdate(
            @Param("merchantId") Long merchantId,
            @Param("cardNumberNormalized") String cardNumberNormalized);
}
