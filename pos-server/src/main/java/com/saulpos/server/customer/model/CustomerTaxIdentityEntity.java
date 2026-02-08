package com.saulpos.server.customer.model;

import com.saulpos.server.identity.model.MerchantEntity;
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
@Table(name = "customer_tax_identity", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_tax_identity_merchant_document",
                columnNames = {"merchant_id", "document_type", "document_value_normalized"}),
        @UniqueConstraint(name = "uk_customer_tax_identity_customer_type",
                columnNames = {"customer_id", "document_type"})
})
public class CustomerTaxIdentityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchant;

    @Column(name = "document_type", nullable = false, length = 40)
    private String documentType;

    @Column(name = "document_value", nullable = false, length = 80)
    private String documentValue;

    @Column(name = "document_value_normalized", nullable = false, length = 80)
    private String documentValueNormalized;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
        this.documentType = this.documentType == null ? null : this.documentType.trim().toUpperCase(Locale.ROOT);
        this.documentValue = this.documentValue == null ? null : this.documentValue.trim();
        this.documentValueNormalized = this.documentValue == null
                ? null
                : this.documentValue.trim().toUpperCase(Locale.ROOT);
    }
}
