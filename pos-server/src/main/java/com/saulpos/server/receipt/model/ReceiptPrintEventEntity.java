package com.saulpos.server.receipt.model;

import com.saulpos.api.receipt.ReceiptPrintStatus;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.sale.model.SaleEntity;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "receipt_print_event")
public class ReceiptPrintEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @Column(name = "receipt_header_id", nullable = false)
    private Long receiptHeaderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_device_id", nullable = false)
    private TerminalDeviceEntity terminalDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccountEntity actorUser;

    @Column(name = "actor_username", nullable = false, length = 80)
    private String actorUsername;

    @Column(name = "is_copy", nullable = false)
    private boolean copy;

    @Column(name = "adapter", nullable = false, length = 40)
    private String adapter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReceiptPrintStatus status;

    @Column(name = "retryable", nullable = false)
    private boolean retryable;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "printed_at", nullable = false, updatable = false)
    private Instant printedAt;

    @PrePersist
    void prePersist() {
        if (this.printedAt == null) {
            this.printedAt = Instant.now();
        }
    }
}
