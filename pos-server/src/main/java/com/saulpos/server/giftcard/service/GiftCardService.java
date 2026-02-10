package com.saulpos.server.giftcard.service;

import com.saulpos.api.giftcard.GiftCardIssueRequest;
import com.saulpos.api.giftcard.GiftCardResponse;
import com.saulpos.api.giftcard.GiftCardRedeemRequest;
import com.saulpos.api.giftcard.GiftCardStatus;
import com.saulpos.api.giftcard.GiftCardTransactionResponse;
import com.saulpos.api.giftcard.GiftCardTransactionType;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.giftcard.model.GiftCardEntity;
import com.saulpos.server.giftcard.model.GiftCardTransactionEntity;
import com.saulpos.server.giftcard.repository.GiftCardRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleReturnEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.sale.repository.SaleReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftCardService {

    private static final int MONEY_SCALE = 2;

    private final GiftCardRepository giftCardRepository;
    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;

    @Transactional
    public GiftCardResponse issue(GiftCardIssueRequest request) {
        MerchantEntity merchant = requireMerchant(request.merchantId());
        CustomerEntity customer = requireCustomer(request.customerId());
        validateCustomerMerchant(customer, merchant);
        validateActiveCustomer(customer);
        String normalizedCardNumber = normalizeCardNumber(request.cardNumber());
        if (giftCardRepository.findByMerchantIdAndCardNumberNormalized(merchant.getId(), normalizedCardNumber).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, "gift card already exists for merchant and card number");
        }

        BigDecimal issuedAmount = normalizeAmount(request.issuedAmount(), "issuedAmount");

        GiftCardEntity giftCard = new GiftCardEntity();
        giftCard.setMerchant(merchant);
        giftCard.setCustomer(customer);
        giftCard.setCardNumber(request.cardNumber().trim());
        giftCard.setCardNumberNormalized(normalizedCardNumber);
        giftCard.setStatus(GiftCardStatus.ACTIVE);
        giftCard.setIssuedAmount(issuedAmount);
        giftCard.setBalanceAmount(issuedAmount);

        GiftCardTransactionEntity issueTx = new GiftCardTransactionEntity();
        issueTx.setTransactionType(GiftCardTransactionType.ISSUE);
        issueTx.setAmount(issuedAmount);
        issueTx.setBalanceBefore(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY));
        issueTx.setBalanceAfter(issuedAmount);
        issueTx.setReference(null);
        issueTx.setNote(normalizeOptional(request.note()));
        giftCard.addTransaction(issueTx);

        return toResponse(giftCardRepository.save(giftCard));
    }

    @Transactional
    public GiftCardResponse redeem(String cardNumber, GiftCardRedeemRequest request) {
        String normalizedCardNumber = normalizeCardNumber(cardNumber);
        BigDecimal redeemAmount = normalizeAmount(request.amount(), "amount");

        GiftCardEntity giftCard = giftCardRepository
                .findByMerchantIdAndCardNumberNormalizedForUpdate(request.merchantId(), normalizedCardNumber)
                .orElseThrow(() -> new BaseException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "gift card not found for merchant and card number"));

        if (giftCard.getStatus() == GiftCardStatus.EXHAUSTED) {
            throw new BaseException(ErrorCode.CONFLICT, "gift card is exhausted");
        }

        BigDecimal balanceBefore = giftCard.getBalanceAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (redeemAmount.compareTo(balanceBefore) > 0) {
            throw new BaseException(ErrorCode.CONFLICT, "gift card balance cannot go below zero");
        }

        SaleEntity sale = null;
        SaleReturnEntity saleReturn = null;
        if (request.saleId() != null) {
            sale = requireSale(request.saleId());
            ensureMerchantContext(giftCard, sale);
        } else {
            saleReturn = requireSaleReturn(request.saleReturnId());
            ensureMerchantContext(giftCard, saleReturn);
        }

        BigDecimal balanceAfter = balanceBefore.subtract(redeemAmount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        giftCard.setBalanceAmount(balanceAfter);
        giftCard.setStatus(balanceAfter.compareTo(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY)) == 0
                ? GiftCardStatus.EXHAUSTED
                : GiftCardStatus.ACTIVE);

        GiftCardTransactionEntity redeemTx = new GiftCardTransactionEntity();
        redeemTx.setTransactionType(GiftCardTransactionType.REDEEM);
        redeemTx.setAmount(redeemAmount);
        redeemTx.setBalanceBefore(balanceBefore);
        redeemTx.setBalanceAfter(balanceAfter);
        redeemTx.setSale(sale);
        redeemTx.setSaleReturn(saleReturn);
        redeemTx.setReference(normalizeOptional(request.reference()));
        redeemTx.setNote(normalizeOptional(request.note()));
        giftCard.addTransaction(redeemTx);

        return toResponse(giftCardRepository.save(giftCard));
    }

    @Transactional(readOnly = true)
    public GiftCardResponse getByCardNumber(Long merchantId, String cardNumber) {
        String normalizedCardNumber = normalizeCardNumber(cardNumber);
        GiftCardEntity giftCard = giftCardRepository.findByMerchantIdAndCardNumberNormalized(merchantId, normalizedCardNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "gift card not found for merchant and card number"));
        return toResponse(giftCard);
    }

    private MerchantEntity requireMerchant(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "merchant not found: " + merchantId));
    }

    private CustomerEntity requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "customer not found: " + customerId));
    }

    private SaleEntity requireSale(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale not found: " + saleId));
    }

    private SaleReturnEntity requireSaleReturn(Long saleReturnId) {
        return saleReturnRepository.findById(saleReturnId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale return not found: " + saleReturnId));
    }

    private void validateCustomerMerchant(CustomerEntity customer, MerchantEntity merchant) {
        if (!customer.getMerchant().getId().equals(merchant.getId())) {
            throw new BaseException(ErrorCode.CONFLICT, "customer merchant does not match gift card merchant");
        }
    }

    private void validateActiveCustomer(CustomerEntity customer) {
        if (!customer.isActive()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "customer is inactive: " + customer.getId());
        }
    }

    private void ensureMerchantContext(GiftCardEntity giftCard, SaleEntity sale) {
        Long giftCardMerchantId = giftCard.getMerchant().getId();
        Long saleMerchantId = sale.getStoreLocation().getMerchant().getId();
        if (!giftCardMerchantId.equals(saleMerchantId)) {
            throw new BaseException(ErrorCode.CONFLICT, "sale merchant does not match gift card merchant");
        }
    }

    private void ensureMerchantContext(GiftCardEntity giftCard, SaleReturnEntity saleReturn) {
        Long giftCardMerchantId = giftCard.getMerchant().getId();
        Long saleMerchantId = saleReturn.getSale().getStoreLocation().getMerchant().getId();
        if (!giftCardMerchantId.equals(saleMerchantId)) {
            throw new BaseException(ErrorCode.CONFLICT, "sale return merchant does not match gift card merchant");
        }
    }

    private BigDecimal normalizeAmount(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, fieldName + " is required");
        }
        BigDecimal normalized = value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY)) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, fieldName + " must be greater than zero");
        }
        return normalized;
    }

    private String normalizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "cardNumber is required");
        }
        String normalized = cardNumber.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "cardNumber is required");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private GiftCardResponse toResponse(GiftCardEntity giftCard) {
        List<GiftCardTransactionResponse> transactions = giftCard.getTransactions().stream()
                .map(transaction -> new GiftCardTransactionResponse(
                        transaction.getId(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getBalanceBefore(),
                        transaction.getBalanceAfter(),
                        transaction.getSale() == null ? null : transaction.getSale().getId(),
                        transaction.getSaleReturn() == null ? null : transaction.getSaleReturn().getId(),
                        transaction.getReference(),
                        transaction.getNote(),
                        transaction.getCreatedAt()))
                .toList();
        return new GiftCardResponse(
                giftCard.getId(),
                giftCard.getMerchant().getId(),
                giftCard.getCustomer().getId(),
                giftCard.getCardNumber(),
                giftCard.getStatus(),
                giftCard.getIssuedAmount(),
                giftCard.getBalanceAmount(),
                giftCard.getIssuedAt(),
                giftCard.getCreatedAt(),
                giftCard.getUpdatedAt(),
                transactions);
    }
}
