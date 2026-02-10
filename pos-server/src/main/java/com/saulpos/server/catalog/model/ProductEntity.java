package com.saulpos.server.catalog.model;

import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductUnitOfMeasure;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_merchant_sku", columnNames = {"merchant_id", "sku"})
})
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_group_id")
    private TaxGroupEntity taxGroup;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(name = "sku_normalized", nullable = false, length = 80)
    private String skuNormalized;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "name_normalized", nullable = false, length = 160)
    private String nameNormalized;

    @Column(name = "base_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_mode", nullable = false, length = 20)
    private ProductSaleMode saleMode = ProductSaleMode.UNIT;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_uom", nullable = false, length = 20)
    private ProductUnitOfMeasure quantityUom = ProductUnitOfMeasure.UNIT;

    @Column(name = "quantity_precision", nullable = false)
    private int quantityPrecision = 0;

    @Column(name = "open_price_min", precision = 14, scale = 2)
    private BigDecimal openPriceMin;

    @Column(name = "open_price_max", precision = 14, scale = 2)
    private BigDecimal openPriceMax;

    @Column(name = "open_price_requires_reason", nullable = false)
    private boolean openPriceRequiresReason = false;

    @Column(name = "lot_tracking_enabled", nullable = false)
    private boolean lotTrackingEnabled = false;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addVariant(ProductVariantEntity variant) {
        variant.setProduct(this);
        variants.add(variant);
    }

    @PrePersist
    void prePersist() {
        syncNormalizedFields();
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        syncNormalizedFields();
        this.updatedAt = Instant.now();
    }

    private void syncNormalizedFields() {
        this.skuNormalized = normalizeForSearch(this.sku);
        this.nameNormalized = normalizeForSearch(this.name);
    }

    private String normalizeForSearch(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
