package com.saulpos.server.inventory.model;

import com.saulpos.server.identity.model.StoreLocationEntity;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stocktake_session")
public class StocktakeSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocationEntity storeLocation;

    @Column(name = "reference_number", nullable = false, length = 80)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StocktakeStatus status;

    @Column(name = "snapshot_at")
    private Instant snapshotAt;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_by", nullable = false, length = 80)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_by", length = 80)
    private String startedBy;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finalized_by", length = 80)
    private String finalizedBy;

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @OneToMany(mappedBy = "stocktakeSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("product.id ASC")
    private List<StocktakeLineEntity> lines = new ArrayList<>();

    public void addLine(StocktakeLineEntity line) {
        line.setStocktakeSession(this);
        lines.add(line);
    }
}
