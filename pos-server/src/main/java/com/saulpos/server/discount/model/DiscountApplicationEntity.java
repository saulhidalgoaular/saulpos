package com.saulpos.server.discount.model;

import com.saulpos.api.discount.DiscountScope;
import com.saulpos.api.discount.DiscountType;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
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
@Table(name = "discount_application")
public class DiscountApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reason_code_id", nullable = false)
    private DiscountReasonCodeEntity reasonCode;

    @Column(name = "context_key", nullable = false, length = 64)
    private String contextKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_scope", nullable = false, length = 20)
    private DiscountScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType type;

    @Column(name = "discount_value", nullable = false, precision = 14, scale = 4)
    private BigDecimal value;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "applied_by_username", nullable = false, length = 80)
    private String appliedByUsername;

    @Column(name = "removed_by_username", length = 80)
    private String removedByUsername;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.appliedAt == null) {
            this.appliedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
