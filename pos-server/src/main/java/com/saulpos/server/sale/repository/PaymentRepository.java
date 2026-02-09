package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
