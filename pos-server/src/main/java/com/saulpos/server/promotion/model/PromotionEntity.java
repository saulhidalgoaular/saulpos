package com.saulpos.server.promotion.model;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_promotion_merchant_code", columnNames = {"merchant_id", "code"})
})
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchant;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionRuleEntity> rules = new ArrayList<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionWindowEntity> windows = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addRule(PromotionRuleEntity rule) {
        rule.setPromotion(this);
        this.rules.add(rule);
    }

    public void addWindow(PromotionWindowEntity window) {
        window.setPromotion(this);
        this.windows.add(window);
    }

    @PrePersist
    void prePersist() {
        normalizeFields();
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        normalizeFields();
        this.updatedAt = Instant.now();
    }

    private void normalizeFields() {
        if (this.code != null) {
            this.code = this.code.trim().toUpperCase(Locale.ROOT);
        }
        if (this.name != null) {
            this.name = this.name.trim();
        }
        if (this.description != null) {
            String normalized = this.description.trim();
            this.description = normalized.isEmpty() ? null : normalized;
        }
    }
}
