package com.saulpos.server.sale.model;

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
@Table(name = "sale_override_event")
public class SaleOverrideEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private SaleCartEntity cart;

    @Column(name = "line_id")
    private Long lineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private SaleOverrideEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_code_id")
    private VoidReasonCodeEntity reasonCode;

    @Column(name = "reason_code", nullable = false, length = 40)
    private String reasonCodeValue;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "before_unit_price", precision = 14, scale = 2)
    private BigDecimal beforeUnitPrice;

    @Column(name = "after_unit_price", precision = 14, scale = 2)
    private BigDecimal afterUnitPrice;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private UserAccountEntity approvedByUser;

    @Column(name = "approved_by_username", length = 80)
    private String approvedByUsername;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccountEntity actorUser;

    @Column(name = "actor_username", length = 80)
    private String actorUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_device_id")
    private TerminalDeviceEntity terminalDevice;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
