package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.PaymentTransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransitionRepository extends JpaRepository<PaymentTransitionEntity, Long> {

    List<PaymentTransitionEntity> findByPaymentIdOrderByCreatedAtAscIdAsc(Long paymentId);
}
