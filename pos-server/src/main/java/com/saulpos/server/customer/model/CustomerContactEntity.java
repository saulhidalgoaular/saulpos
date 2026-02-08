package com.saulpos.server.customer.model;

import com.saulpos.api.customer.CustomerContactType;
import com.saulpos.server.identity.model.MerchantEntity;
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
@Table(name = "customer_contact", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_contact_customer_type_value",
                columnNames = {"customer_id", "contact_type", "contact_value_normalized"})
})
public class CustomerContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    private CustomerContactType contactType;

    @Column(name = "contact_value", nullable = false, length = 120)
    private String contactValue;

    @Column(name = "contact_value_normalized", nullable = false, length = 120)
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

        if (this.contactType == CustomerContactType.EMAIL) {
            this.contactValueNormalized = this.contactValue.toLowerCase(Locale.ROOT);
            return;
        }

        this.contactValueNormalized = this.contactValue
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .toUpperCase(Locale.ROOT);
    }
}
