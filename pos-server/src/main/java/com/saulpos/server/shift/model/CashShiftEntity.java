package com.saulpos.server.shift.model;

import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.security.model.UserAccountEntity;
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
@Table(name = "cash_shift")
public class CashShiftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cashier_user_id", nullable = false)
    private UserAccountEntity cashierUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_device_id", nullable = false)
    private TerminalDeviceEntity terminalDevice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CashShiftStatus status = CashShiftStatus.OPEN;

    @Column(name = "opening_cash", nullable = false, precision = 14, scale = 2)
    private BigDecimal openingCash;

    @Column(name = "total_paid_in", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPaidIn = BigDecimal.ZERO;

    @Column(name = "total_paid_out", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPaidOut = BigDecimal.ZERO;

    @Column(name = "expected_close_cash", precision = 14, scale = 2)
    private BigDecimal expectedCloseCash;

    @Column(name = "counted_close_cash", precision = 14, scale = 2)
    private BigDecimal countedCloseCash;

    @Column(name = "variance_cash", precision = 14, scale = 2)
    private BigDecimal varianceCash;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.openedAt == null) {
            this.openedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
