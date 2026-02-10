package com.saulpos.server.receipt.model;

import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "no_sale_drawer_event")
public class NoSaleDrawerEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_device_id")
    private TerminalDeviceEntity terminalDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_user_id")
    private UserAccountEntity cashierUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccountEntity actorUser;

    @Column(name = "actor_username", length = 80)
    private String actorUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private UserAccountEntity approvedByUser;

    @Column(name = "approved_by_username", length = 80)
    private String approvedByUsername;

    @Column(name = "reason_code", nullable = false, length = 40)
    private String reasonCode;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "reference_number", length = 120)
    private String referenceNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
