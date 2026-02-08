package com.saulpos.server.catalog.model;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_barcode", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_barcode", columnNames = {"barcode"})
})
public class ProductBarcodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariantEntity variant;

    @Column(nullable = false, length = 64)
    private String barcode;

    @Column(name = "barcode_normalized", nullable = false, length = 64)
    private String barcodeNormalized;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        this.barcodeNormalized = normalizeForSearch(this.barcode);
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.barcodeNormalized = normalizeForSearch(this.barcode);
        this.updatedAt = Instant.now();
    }

    private String normalizeForSearch(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
