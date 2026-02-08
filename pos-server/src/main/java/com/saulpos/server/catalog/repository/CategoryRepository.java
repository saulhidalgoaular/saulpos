package com.saulpos.server.catalog.repository;

import com.saulpos.server.catalog.model.CategoryEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByIdAndMerchantId(Long id, Long merchantId);

    @EntityGraph(attributePaths = {"parent"})
    List<CategoryEntity> findByMerchantId(Long merchantId);
}
