package com.saulpos.server.catalog;

import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.api.catalog.ProductUnitOfMeasure;
import com.saulpos.server.catalog.service.ProductSaleModePolicyValidator;
import com.saulpos.server.error.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSaleModePolicyValidatorTest {

    private ProductSaleModePolicyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProductSaleModePolicyValidator();
    }

    @Test
    void defaultsUnitModeWhenFieldsAreMissing() {
        ProductSaleModePolicyValidator.NormalizedPolicy policy = validator.normalizePolicy(
                null, null, null, null, null, null);

        assertThat(policy.saleMode()).isEqualTo(ProductSaleMode.UNIT);
        assertThat(policy.quantityUom()).isEqualTo(ProductUnitOfMeasure.UNIT);
        assertThat(policy.quantityPrecision()).isEqualTo(0);
        assertThat(policy.openPriceMin()).isNull();
        assertThat(policy.openPriceMax()).isNull();
        assertThat(policy.openPriceRequiresReason()).isFalse();
    }

    @Test
    void rejectsWeightWithoutDecimalPrecision() {
        assertThatThrownBy(() -> validator.normalizePolicy(
                ProductSaleMode.WEIGHT,
                ProductUnitOfMeasure.KILOGRAM,
                0,
                null,
                null,
                false))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("WEIGHT saleMode requires quantityPrecision between 1 and 3");
    }

    @Test
    void rejectsOpenPriceRangeWhenMinGreaterThanMax() {
        assertThatThrownBy(() -> validator.normalizePolicy(
                ProductSaleMode.OPEN_PRICE,
                ProductUnitOfMeasure.UNIT,
                0,
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                true))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("openPriceMin must be less than or equal to openPriceMax");
    }

    @Test
    void validatesOpenPriceEntryBoundsAndReason() {
        BigDecimal enteredPrice = validator.validateOpenPriceEntry(
                new BigDecimal("7.995"),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                true,
                "Manager approved");

        assertThat(enteredPrice).isEqualByComparingTo("8.00");
    }

    @Test
    void rejectsOpenPriceEntryWhenReasonIsRequiredAndMissing() {
        assertThatThrownBy(() -> validator.validateOpenPriceEntry(
                new BigDecimal("8.00"),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                true,
                "   "))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("reason is required for this product");
    }
}
