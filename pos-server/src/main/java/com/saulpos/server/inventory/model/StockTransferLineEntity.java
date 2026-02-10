package com.saulpos.server.inventory.model;

import com.saulpos.server.catalog.model.ProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_transfer_line")
public class StockTransferLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_transfer_id", nullable = false)
    private StockTransferEntity stockTransfer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "requested_quantity", nullable = false, precision = 13, scale = 3)
    private BigDecimal requestedQuantity = BigDecimal.ZERO;

    @Column(name = "shipped_quantity", precision = 13, scale = 3)
    private BigDecimal shippedQuantity;

    @Column(name = "received_quantity", nullable = false, precision = 13, scale = 3)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;
}
