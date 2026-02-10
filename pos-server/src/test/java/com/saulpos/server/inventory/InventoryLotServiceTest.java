package com.saulpos.server.inventory;

import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.inventory.model.InventoryLotBalanceEntity;
import com.saulpos.server.inventory.model.InventoryLotEntity;
import com.saulpos.server.inventory.repository.InventoryLotBalanceRepository;
import com.saulpos.server.inventory.repository.InventoryLotRepository;
import com.saulpos.server.inventory.repository.InventoryMovementLotRepository;
import com.saulpos.server.inventory.service.InventoryBalanceCalculator;
import com.saulpos.server.inventory.service.InventoryLotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryLotServiceTest {

    @Mock
    private InventoryLotRepository inventoryLotRepository;

    @Mock
    private InventoryLotBalanceRepository inventoryLotBalanceRepository;

    @Mock
    private InventoryMovementLotRepository inventoryMovementLotRepository;

    private InventoryLotService inventoryLotService;
    private StoreLocationEntity storeLocation;
    private ProductEntity lotTrackedProduct;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-10T12:00:00Z"), ZoneOffset.UTC);
        inventoryLotService = new InventoryLotService(
                inventoryLotRepository,
                inventoryLotBalanceRepository,
                inventoryMovementLotRepository,
                new InventoryBalanceCalculator(),
                clock);

        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(1L);

        storeLocation = new StoreLocationEntity();
        storeLocation.setId(10L);
        storeLocation.setMerchant(merchant);

        lotTrackedProduct = new ProductEntity();
        lotTrackedProduct.setId(100L);
        lotTrackedProduct.setMerchant(merchant);
        lotTrackedProduct.setLotTrackingEnabled(true);
    }

    @Test
    void allocateSaleLotsUsesFefoByEarliestExpiry() {
        InventoryLotBalanceEntity first = balance(1L, "LOT-1", LocalDate.parse("2026-02-11"), "1.000");
        InventoryLotBalanceEntity second = balance(2L, "LOT-2", LocalDate.parse("2026-02-14"), "5.000");

        when(inventoryLotBalanceRepository.findPositiveBalancesForSaleForUpdate(storeLocation.getId(), lotTrackedProduct.getId()))
                .thenReturn(List.of(first, second));

        List<InventoryLotService.LotAllocation> allocations = inventoryLotService.allocateSaleLots(
                storeLocation,
                lotTrackedProduct,
                new BigDecimal("2.000"),
                false);

        assertThat(allocations).hasSize(2);
        assertThat(allocations.get(0).inventoryLot().getLotCode()).isEqualTo("LOT-1");
        assertThat(allocations.get(0).quantity()).isEqualByComparingTo("1.000");
        assertThat(allocations.get(1).inventoryLot().getLotCode()).isEqualTo("LOT-2");
        assertThat(allocations.get(1).quantity()).isEqualByComparingTo("1.000");

        assertThat(first.getQuantityOnHand()).isEqualByComparingTo("0.000");
        assertThat(second.getQuantityOnHand()).isEqualByComparingTo("4.000");
    }

    @Test
    void allocateSaleLotsPlacesNoExpiryAfterDatedLots() {
        InventoryLotBalanceEntity dated = balance(3L, "LOT-DATED", LocalDate.parse("2026-02-20"), "1.000");
        InventoryLotBalanceEntity noExpiry = balance(4L, "LOT-NO-EXP", null, "3.000");

        when(inventoryLotBalanceRepository.findPositiveBalancesForSaleForUpdate(storeLocation.getId(), lotTrackedProduct.getId()))
                .thenReturn(List.of(dated, noExpiry));

        List<InventoryLotService.LotAllocation> allocations = inventoryLotService.allocateSaleLots(
                storeLocation,
                lotTrackedProduct,
                new BigDecimal("2.000"),
                false);

        assertThat(allocations).hasSize(2);
        assertThat(allocations.get(0).inventoryLot().getLotCode()).isEqualTo("LOT-DATED");
        assertThat(allocations.get(0).quantity()).isEqualByComparingTo("1.000");
        assertThat(allocations.get(1).inventoryLot().getLotCode()).isEqualTo("LOT-NO-EXP");
        assertThat(allocations.get(1).quantity()).isEqualByComparingTo("1.000");
    }

    @Test
    void allocateSaleLotsBlocksExpiredLotsWithoutOverridePermission() {
        InventoryLotBalanceEntity expired = balance(5L, "LOT-EXP", LocalDate.parse("2026-02-09"), "2.000");

        when(inventoryLotBalanceRepository.findPositiveBalancesForSaleForUpdate(storeLocation.getId(), lotTrackedProduct.getId()))
                .thenReturn(List.of(expired));

        assertThatThrownBy(() -> inventoryLotService.allocateSaleLots(
                storeLocation,
                lotTrackedProduct,
                new BigDecimal("1.000"),
                false))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> {
                    BaseException base = (BaseException) ex;
                    assertThat(base.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                });
    }

    private InventoryLotBalanceEntity balance(Long lotId, String lotCode, LocalDate expiryDate, String quantity) {
        InventoryLotEntity lot = new InventoryLotEntity();
        lot.setId(lotId);
        lot.setStoreLocation(storeLocation);
        lot.setProduct(lotTrackedProduct);
        lot.setLotCode(lotCode);
        lot.setExpiryDate(expiryDate);

        InventoryLotBalanceEntity balance = new InventoryLotBalanceEntity();
        balance.setInventoryLot(lot);
        balance.setInventoryLotId(lotId);
        balance.setQuantityOnHand(new BigDecimal(quantity));
        return balance;
    }
}
