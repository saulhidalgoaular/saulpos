package com.saulpos.server.fiscal.model;

import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleReturnEntity;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fiscal_document",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fiscal_document_sale_type", columnNames = {"sale_id", "document_type"}),
                @UniqueConstraint(name = "uk_fiscal_document_sale_return_type", columnNames = {"sale_return_id", "document_type"})
        })
public class FiscalDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private SaleEntity sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_return_id")
    private SaleReturnEntity saleReturn;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private FiscalDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FiscalDocumentStatus status;

    @Column(name = "provider_code", nullable = false, length = 40)
    private String providerCode;

    @Column(name = "external_document_id", length = 120)
    private String externalDocumentId;

    @Column(name = "request_reference", nullable = false, length = 80)
    private String requestReference;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
