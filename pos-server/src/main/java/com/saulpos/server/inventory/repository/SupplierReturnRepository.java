package com.saulpos.server.inventory.repository;

import com.saulpos.server.inventory.model.SupplierReturnEntity;
import com.saulpos.server.inventory.model.SupplierReturnStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface SupplierReturnRepository extends JpaRepository<SupplierReturnEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct supplierReturn
            from SupplierReturnEntity supplierReturn
            join fetch supplierReturn.supplier
            join fetch supplierReturn.storeLocation
            left join fetch supplierReturn.lines line
            left join fetch line.product
            where supplierReturn.id = :id
            """)
    Optional<SupplierReturnEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select distinct supplierReturn
            from SupplierReturnEntity supplierReturn
            join fetch supplierReturn.supplier
            join fetch supplierReturn.storeLocation
            left join fetch supplierReturn.lines line
            left join fetch line.product
            where supplierReturn.id = :id
            """)
    Optional<SupplierReturnEntity> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select coalesce(sum(line.returnQuantity), 0)
            from SupplierReturnLineEntity line
            join line.supplierReturn supplierReturn
            where supplierReturn.supplier.id = :supplierId
              and supplierReturn.storeLocation.id = :storeLocationId
              and line.product.id = :productId
              and supplierReturn.status = :status
            """)
    BigDecimal sumReturnQuantityBySupplierStoreAndProduct(@Param("supplierId") Long supplierId,
                                                          @Param("storeLocationId") Long storeLocationId,
                                                          @Param("productId") Long productId,
                                                          @Param("status") SupplierReturnStatus status);
}
