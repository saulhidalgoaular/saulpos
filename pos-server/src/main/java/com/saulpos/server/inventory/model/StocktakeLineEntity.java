package com.saulpos.server.inventory.model;

import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.sale.model.InventoryMovementEntity;
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
@Table(name = "stocktake_line")
public class StocktakeLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stocktake_session_id", nullable = false)
    private StocktakeSessionEntity stocktakeSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "expected_quantity", nullable = false, precision = 13, scale = 3)
    private BigDecimal expectedQuantity = BigDecimal.ZERO;

    @Column(name = "counted_quantity", precision = 13, scale = 3)
    private BigDecimal countedQuantity;

    @Column(name = "variance_quantity", precision = 13, scale = 3)
    private BigDecimal varianceQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_movement_id")
    private InventoryMovementEntity inventoryMovement;
}
