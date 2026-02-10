package com.saulpos.server.giftcard.model;

import com.saulpos.api.giftcard.GiftCardStatus;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.identity.model.MerchantEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gift_card", uniqueConstraints = {
        @UniqueConstraint(name = "uk_gift_card_merchant_number", columnNames = {"merchant_id", "card_number_normalized"})
})
public class GiftCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "card_number", nullable = false, length = 40)
    private String cardNumber;

    @Column(name = "card_number_normalized", nullable = false, length = 40)
    private String cardNumberNormalized;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GiftCardStatus status = GiftCardStatus.ACTIVE;

    @Column(name = "issued_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal issuedAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @OneToMany(mappedBy = "giftCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<GiftCardTransactionEntity> transactions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addTransaction(GiftCardTransactionEntity transaction) {
        transaction.setGiftCard(this);
        transactions.add(transaction);
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (issuedAt == null) {
            issuedAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
