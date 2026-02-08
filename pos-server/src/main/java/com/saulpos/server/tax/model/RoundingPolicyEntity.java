package com.saulpos.server.tax.model;

import com.saulpos.api.tax.RoundingMethod;
import com.saulpos.api.tax.TenderType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rounding_policy", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rounding_policy_store_tender", columnNames = {"store_location_id", "tender_type"})
})
public class RoundingPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "tender_type", nullable = false, length = 20)
    private TenderType tenderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rounding_method", nullable = false, length = 20)
    private RoundingMethod roundingMethod;

    @Column(name = "increment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal incrementAmount;

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
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
