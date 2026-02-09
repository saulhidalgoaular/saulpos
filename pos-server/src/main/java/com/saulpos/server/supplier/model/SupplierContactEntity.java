package com.saulpos.server.supplier.model;

import com.saulpos.api.supplier.SupplierContactType;
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

import java.time.Instant;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "supplier_contact", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_contact_supplier_type_value",
                columnNames = {"supplier_id", "contact_type", "contact_value_normalized"})
})
public class SupplierContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    private SupplierContactType contactType;

    @Column(name = "contact_value", nullable = false, length = 160)
    private String contactValue;

    @Column(name = "contact_value_normalized", nullable = false, length = 160)
    private String contactValueNormalized;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

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
        this.contactValue = this.contactValue == null ? null : this.contactValue.trim();
        if (this.contactValue == null) {
            this.contactValueNormalized = null;
            return;
        }

        if (this.contactType == SupplierContactType.EMAIL) {
            this.contactValueNormalized = this.contactValue.toLowerCase(Locale.ROOT);
            return;
        }

        this.contactValueNormalized = this.contactValue
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
    }
}
