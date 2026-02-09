package com.saulpos.server.sale;

import com.saulpos.api.catalog.ProductSaleMode;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.sale.service.CartLinePolicyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartLinePolicyValidatorTest {

    private CartLinePolicyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CartLinePolicyValidator();
    }

    @Test
    void unitAndOpenPriceRequireWholeQuantities() {
        ProductEntity unitProduct = product(1L, ProductSaleMode.UNIT, 0);
        ProductEntity openPriceProduct = product(2L, ProductSaleMode.OPEN_PRICE, 0);

        assertThatThrownBy(() -> validator.normalizeQuantity(unitProduct, new BigDecimal("1.250")))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("whole quantity");

        assertThatThrownBy(() -> validator.normalizeQuantity(openPriceProduct, new BigDecimal("2.100")))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("whole quantity");

        assertThat(validator.normalizeQuantity(unitProduct, new BigDecimal("3")))
                .isEqualByComparingTo(new BigDecimal("3.000"));
    }

    @Test
    void weightModeEnforcesProductPrecision() {
        ProductEntity weightProduct = product(3L, ProductSaleMode.WEIGHT, 2);

        assertThat(validator.normalizeQuantity(weightProduct, new BigDecimal("1.25")))
                .isEqualByComparingTo(new BigDecimal("1.250"));

        assertThatThrownBy(() -> validator.normalizeQuantity(weightProduct, new BigDecimal("1.257")))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("precision exceeds");
    }

    @Test
    void lineKeyNormalizationIsUppercasedAndTrimmed() {
        assertThat(validator.normalizeLineKey("  line-abc  ")).isEqualTo("LINE-ABC");
        assertThat(validator.normalizeLineKey("   ")).isNull();
        assertThat(validator.normalizeLineKey(null)).isNull();
    }

    private ProductEntity product(Long id, ProductSaleMode saleMode, int quantityPrecision) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setSaleMode(saleMode);
        product.setQuantityPrecision(quantityPrecision);
        return product;
    }
}
