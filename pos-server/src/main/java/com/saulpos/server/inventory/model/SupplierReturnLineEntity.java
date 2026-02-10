package com.saulpos.server.inventory.model;

import com.saulpos.server.catalog.model.ProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "supplier_return_line")
public class SupplierReturnLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_return_id", nullable = false)
    private SupplierReturnEntity supplierReturn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "return_quantity", nullable = false, precision = 13, scale = 3)
    private BigDecimal returnQuantity = BigDecimal.ZERO;

    @Column(name = "unit_cost", nullable = false, precision = 16, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "inventory_movement_id")
    private Long inventoryMovementId;
}
