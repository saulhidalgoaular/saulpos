package com.saulpos.server.inventory.model;

import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory_product_cost")
public class InventoryProductCostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "weighted_average_cost", nullable = false, precision = 13, scale = 4)
    private BigDecimal weightedAverageCost = BigDecimal.ZERO;

    @Column(name = "last_cost", nullable = false, precision = 13, scale = 4)
    private BigDecimal lastCost = BigDecimal.ZERO;

    @Column(name = "last_receipt_reference", length = 80)
    private String lastReceiptReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_movement_id")
    private InventoryMovementEntity lastMovement;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
