package com.saulpos.server.sale.repository;

import com.saulpos.server.sale.model.SaleReturnEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturnEntity, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.saleLine", "lines.saleLine.product"})
    List<SaleReturnEntity> findBySaleIdOrderByCreatedAtAscIdAsc(Long saleId);
}
