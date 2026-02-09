package com.saulpos.server.sale;

import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.sale.service.SaleCartService;
import com.saulpos.server.sale.service.SaleCheckoutService;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import com.saulpos.server.tax.repository.StoreTaxRuleRepository;
import com.saulpos.server.tax.repository.TaxGroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SaleCheckoutConcurrencyIntegrationTest {

    @Autowired
    private SaleCartService saleCartService;

    @Autowired
    private SaleCheckoutService saleCheckoutService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private TerminalDeviceRepository terminalDeviceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TaxGroupRepository taxGroupRepository;

    @Autowired
    private StoreTaxRuleRepository storeTaxRuleRepository;

    private ExecutorService executorService;

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);

        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_override_event");
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM customer_group_assignment");
        jdbcTemplate.execute("DELETE FROM customer_group");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM promotion_window");
        jdbcTemplate.execute("DELETE FROM promotion_rule");
        jdbcTemplate.execute("DELETE FROM promotion");
        jdbcTemplate.execute("DELETE FROM discount_application");
        jdbcTemplate.execute("DELETE FROM discount_reason_code");
        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
        jdbcTemplate.execute("DELETE FROM rounding_policy");
        jdbcTemplate.execute("DELETE FROM store_tax_rule");
        jdbcTemplate.execute("DELETE FROM tax_group");
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM store_user_assignment");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("cashier-checkout-race");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-G2-RACE");
        merchant.setName("Merchant G2 Race");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-G2-RACE");
        storeLocation.setName("Store G2 Race");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-G2-RACE");
        terminal.setName("Terminal G2 Race");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-G2-RACE");
        category.setName("Category G2 Race");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-RACE");
        taxGroup.setName("VAT 10 Race");
        taxGroup.setTaxRatePercent(new BigDecimal("10.0000"));
        taxGroup.setZeroRated(false);
        taxGroup.setActive(true);
        taxGroup = taxGroupRepository.save(taxGroup);

        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(storeLocation);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(TaxMode.EXCLUSIVE);
        taxRule.setExempt(false);
        taxRule.setActive(true);
        storeTaxRuleRepository.save(taxRule);

        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setTaxGroup(taxGroup);
        product.setSku("SKU-G2-RACE");
        product.setName("Product G2 Race");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("Checkout race item");
        product.setActive(true);
        productId = productRepository.save(product).getId();
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void onlyOneCheckoutSucceedsWhenSameCartIsProcessedConcurrently() throws Exception {
        Long cartId = saleCartService.createCart(new SaleCartCreateRequest(
                cashierUserId,
                storeLocationId,
                terminalDeviceId,
                Instant.parse("2026-02-09T10:00:00Z"))).id();

        saleCartService.addLine(cartId, new SaleCartAddLineRequest(
                "scan-g2-race",
                productId,
                new BigDecimal("2.000"),
                null,
                null));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        AtomicInteger unexpectedFailureCount = new AtomicInteger();

        Runnable checkoutTask = () -> {
            try {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                saleCheckoutService.checkout(new SaleCheckoutRequest(
                        cartId,
                        cashierUserId,
                        terminalDeviceId,
                        List.of(new SaleCheckoutPaymentRequest(
                                TenderType.CASH,
                                new BigDecimal("11.00"),
                                new BigDecimal("11.00"),
                                null))));
                successCount.incrementAndGet();
            } catch (BaseException exception) {
                if (exception.getErrorCode() == ErrorCode.CONFLICT) {
                    conflictCount.incrementAndGet();
                } else {
                    unexpectedFailureCount.incrementAndGet();
                }
            } catch (Exception exception) {
                unexpectedFailureCount.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        executorService.submit(checkoutTask);
        executorService.submit(checkoutTask);

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(20, TimeUnit.SECONDS)).isTrue();

        Integer saleCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM sale WHERE cart_id = ?",
                Integer.class,
                cartId);
        Integer saleLineCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM sale_line sl JOIN sale s ON s.id = sl.sale_id WHERE s.cart_id = ?",
                Integer.class,
                cartId);
        Integer movementCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM inventory_movement im JOIN sale s ON s.id = im.sale_id WHERE s.cart_id = ?",
                Integer.class,
                cartId);
        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM payment WHERE cart_id = ?",
                Integer.class,
                cartId);
        Integer receiptCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM receipt_header",
                Integer.class);
        String cartStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM sale_cart WHERE id = ?",
                String.class,
                cartId);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);
        assertThat(unexpectedFailureCount.get()).isZero();
        assertThat(saleCount).isEqualTo(1);
        assertThat(saleLineCount).isEqualTo(1);
        assertThat(movementCount).isEqualTo(1);
        assertThat(paymentCount).isEqualTo(1);
        assertThat(receiptCount).isEqualTo(1);
        assertThat(cartStatus).isEqualTo("CHECKED_OUT");
    }
}
