package com.saulpos.server.inventory.model;

import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_adjustment")
public class StockAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "quantity_delta", nullable = false, precision = 13, scale = 3)
    private BigDecimal quantityDelta = BigDecimal.ZERO;

    @Column(name = "reason_code", nullable = false, length = 40)
    private String reasonCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StockAdjustmentStatus status;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Column(name = "reference_number", nullable = false, length = 80)
    private String referenceNumber;

    @Column(name = "request_note", length = 255)
    private String requestNote;

    @Column(name = "approval_note", length = 255)
    private String approvalNote;

    @Column(name = "post_note", length = 255)
    private String postNote;

    @Column(name = "requested_by", nullable = false, length = 80)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "approved_by", length = 80)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "posted_by", length = 80)
    private String postedBy;

    @Column(name = "posted_at")
    private Instant postedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_movement_id")
    private InventoryMovementEntity inventoryMovement;
}
