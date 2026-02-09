package com.saulpos.server.supplier.model;

import com.saulpos.server.identity.model.MerchantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "supplier", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_merchant_code", columnNames = {"merchant_id", "code"}),
        @UniqueConstraint(name = "uk_supplier_merchant_tax_identifier",
                columnNames = {"merchant_id", "tax_identifier_normalized"})
})
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchant;

    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "tax_identifier", length = 80)
    private String taxIdentifier;

    @Column(name = "tax_identifier_normalized", length = 80)
    private String taxIdentifierNormalized;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SupplierContactEntity> contacts = new LinkedHashSet<>();

    @OneToOne(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private SupplierTermsEntity terms;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addContact(SupplierContactEntity contact) {
        contact.setSupplier(this);
        this.contacts.add(contact);
    }

    public void setTerms(SupplierTermsEntity terms) {
        if (this.terms != null) {
            this.terms.setSupplier(null);
        }
        this.terms = terms;
        if (terms != null) {
            terms.setSupplier(this);
        }
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
        this.code = this.code == null ? null : this.code.trim().toUpperCase(Locale.ROOT);
        this.name = this.name == null ? null : this.name.trim();
        this.taxIdentifier = this.taxIdentifier == null ? null : this.taxIdentifier.trim();
        if (this.taxIdentifier == null || this.taxIdentifier.isBlank()) {
            this.taxIdentifier = null;
            this.taxIdentifierNormalized = null;
            return;
        }
        this.taxIdentifierNormalized = this.taxIdentifier
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
    }
}
