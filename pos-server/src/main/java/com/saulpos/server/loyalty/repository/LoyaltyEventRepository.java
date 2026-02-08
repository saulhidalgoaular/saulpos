package com.saulpos.server.loyalty.repository;

import com.saulpos.server.loyalty.model.LoyaltyEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyEventRepository extends JpaRepository<LoyaltyEventEntity, Long> {
}
