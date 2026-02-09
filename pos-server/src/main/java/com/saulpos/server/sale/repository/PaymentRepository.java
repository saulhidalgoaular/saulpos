package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.PaymentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("""
            select payment
            from PaymentEntity payment
            left join fetch payment.allocations
            where payment.cart.id = :cartId
            """)
    Optional<PaymentEntity> findByCartIdWithAllocations(@Param("cartId") Long cartId);

    @Query("""
            select payment
            from PaymentEntity payment
            left join fetch payment.allocations
            where payment.id = :paymentId
            """)
    Optional<PaymentEntity> findByIdWithAllocations(@Param("paymentId") Long paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from PaymentEntity payment where payment.id = :paymentId")
    Optional<PaymentEntity> findByIdForUpdate(@Param("paymentId") Long paymentId);
}
