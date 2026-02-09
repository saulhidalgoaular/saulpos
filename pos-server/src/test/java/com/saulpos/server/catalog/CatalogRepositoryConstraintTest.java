package com.saulpos.server.catalog;

import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class CatalogRepositoryConstraintTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM store_user_assignment");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
    }

    @Test
    void skuMustBeUniquePerMerchantAtRepositoryLevel() {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-DB-C1");
        merchant.setName("Merchant DB C1");
        merchant.setActive(true);
        MerchantEntity savedMerchant = merchantRepository.saveAndFlush(merchant);

        ProductEntity first = new ProductEntity();
        first.setMerchant(savedMerchant);
        first.setSku("SKU-REPO-001");
        first.setName("First Product");
        first.setDescription("first");
        first.setActive(true);
        productRepository.saveAndFlush(first);

        ProductEntity duplicate = new ProductEntity();
        duplicate.setMerchant(savedMerchant);
        duplicate.setSku("SKU-REPO-001");
        duplicate.setName("Duplicate Product");
        duplicate.setDescription("duplicate");
        duplicate.setActive(true);

        assertThatThrownBy(() -> productRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
