package com.saulpos.server.giftcard;

import com.saulpos.api.giftcard.GiftCardIssueRequest;
import com.saulpos.api.giftcard.GiftCardRedeemRequest;
import com.saulpos.api.giftcard.GiftCardResponse;
import com.saulpos.api.giftcard.GiftCardStatus;
import com.saulpos.api.giftcard.GiftCardTransactionType;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.giftcard.model.GiftCardEntity;
import com.saulpos.server.giftcard.repository.GiftCardRepository;
import com.saulpos.server.giftcard.service.GiftCardService;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.sale.repository.SaleReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftCardServiceTest {

    @Mock
    private GiftCardRepository giftCardRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleReturnRepository saleReturnRepository;

    private GiftCardService giftCardService;

    @BeforeEach
    void setUp() {
        giftCardService = new GiftCardService(
                giftCardRepository,
                merchantRepository,
                customerRepository,
                saleRepository,
                saleReturnRepository);
    }

    @Test
    void issueCreatesCardWithOpeningTransaction() {
        MerchantEntity merchant = merchant(10L);
        CustomerEntity customer = customer(30L, merchant);
        when(merchantRepository.findById(10L)).thenReturn(Optional.of(merchant));
        when(customerRepository.findById(30L)).thenReturn(Optional.of(customer));
        when(giftCardRepository.findByMerchantIdAndCardNumberNormalized(10L, "GC-100")).thenReturn(Optional.empty());
        when(giftCardRepository.save(any(GiftCardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftCardResponse response = giftCardService.issue(new GiftCardIssueRequest(
                10L,
                30L,
                "gc-100",
                new BigDecimal("100.00"),
                "first issue"));

        assertThat(response.status()).isEqualTo(GiftCardStatus.ACTIVE);
        assertThat(response.balanceAmount()).isEqualByComparingTo("100.00");
        assertThat(response.transactions()).hasSize(1);
        assertThat(response.transactions().get(0).transactionType()).isEqualTo(GiftCardTransactionType.ISSUE);
        assertThat(response.transactions().get(0).amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void redeemRejectsWhenBalanceWouldGoBelowZero() {
        GiftCardEntity giftCard = giftCard(10L, 30L, "GC-200", "GC-200", new BigDecimal("25.00"), new BigDecimal("5.00"));
        when(giftCardRepository.findByMerchantIdAndCardNumberNormalizedForUpdate(10L, "GC-200"))
                .thenReturn(Optional.of(giftCard));

        assertThatThrownBy(() -> giftCardService.redeem("GC-200", new GiftCardRedeemRequest(
                10L,
                new BigDecimal("6.00"),
                900L,
                null,
                "SALE-900",
                null)))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void redeemLinksTransactionToSaleContextAndUpdatesBalance() {
        MerchantEntity merchant = merchant(10L);
        GiftCardEntity giftCard = giftCard(10L, 30L, "GC-300", "GC-300", new BigDecimal("50.00"), new BigDecimal("20.00"));
        SaleEntity sale = new SaleEntity();
        sale.setId(900L);
        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        sale.setStoreLocation(storeLocation);

        when(giftCardRepository.findByMerchantIdAndCardNumberNormalizedForUpdate(eq(10L), eq("GC-300")))
                .thenReturn(Optional.of(giftCard));
        when(saleRepository.findById(900L)).thenReturn(Optional.of(sale));
        when(giftCardRepository.save(any(GiftCardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftCardResponse response = giftCardService.redeem("GC-300", new GiftCardRedeemRequest(
                10L,
                new BigDecimal("7.50"),
                900L,
                null,
                "SALE-900",
                "redeem"));

        assertThat(response.balanceAmount()).isEqualByComparingTo("12.50");
        assertThat(response.status()).isEqualTo(GiftCardStatus.ACTIVE);
        assertThat(response.transactions()).hasSize(1);
        assertThat(response.transactions().get(0).transactionType()).isEqualTo(GiftCardTransactionType.REDEEM);
        assertThat(response.transactions().get(0).saleId()).isEqualTo(900L);
        assertThat(response.transactions().get(0).saleReturnId()).isNull();
    }

    private MerchantEntity merchant(Long merchantId) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(merchantId);
        merchant.setCode("MER-" + merchantId);
        merchant.setName("Merchant " + merchantId);
        merchant.setActive(true);
        return merchant;
    }

    private CustomerEntity customer(Long customerId, MerchantEntity merchant) {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);
        customer.setMerchant(merchant);
        customer.setActive(true);
        return customer;
    }

    private GiftCardEntity giftCard(Long merchantId,
                                    Long customerId,
                                    String cardNumber,
                                    String normalizedCardNumber,
                                    BigDecimal issuedAmount,
                                    BigDecimal balanceAmount) {
        MerchantEntity merchant = merchant(merchantId);
        CustomerEntity customer = customer(customerId, merchant);

        GiftCardEntity giftCard = new GiftCardEntity();
        giftCard.setMerchant(merchant);
        giftCard.setCustomer(customer);
        giftCard.setCardNumber(cardNumber);
        giftCard.setCardNumberNormalized(normalizedCardNumber);
        giftCard.setIssuedAmount(issuedAmount);
        giftCard.setBalanceAmount(balanceAmount);
        giftCard.setStatus(GiftCardStatus.ACTIVE);
        return giftCard;
    }
}
