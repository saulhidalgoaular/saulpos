package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.PriceBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceBookRepository extends JpaRepository<PriceBookEntity, Long> {
}
