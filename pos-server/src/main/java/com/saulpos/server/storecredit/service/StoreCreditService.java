package com.saulpos.server.storecredit.service;

import com.saulpos.api.storecredit.StoreCreditAccountResponse;
import com.saulpos.api.storecredit.StoreCreditIssueRequest;
import com.saulpos.api.storecredit.StoreCreditRedeemRequest;
import com.saulpos.api.storecredit.StoreCreditTransactionResponse;
import com.saulpos.api.storecredit.StoreCreditTransactionType;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleReturnEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.sale.repository.SaleReturnRepository;
import com.saulpos.server.storecredit.model.StoreCreditAccountEntity;
import com.saulpos.server.storecredit.model.StoreCreditTransactionEntity;
import com.saulpos.server.storecredit.repository.StoreCreditAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreCreditService {

    private static final int MONEY_SCALE = 2;

    private final StoreCreditAccountRepository storeCreditAccountRepository;
    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;

    @Transactional
    public StoreCreditAccountResponse issue(StoreCreditIssueRequest request) {
        MerchantEntity merchant = requireMerchant(request.merchantId());
        CustomerEntity customer = requireCustomer(request.customerId());
        validateCustomerMerchant(customer, merchant);
        validateActiveCustomer(customer);

        SaleReturnEntity saleReturn = requireSaleReturn(request.saleReturnId());
        ensureMerchantContext(merchant, saleReturn);
        ensureSaleReturnCustomerContext(customer, saleReturn);

        BigDecimal amount = normalizeAmount(request.amount(), "amount");

        StoreCreditAccountEntity account = storeCreditAccountRepository
                .findByMerchantIdAndCustomerIdForUpdate(merchant.getId(), customer.getId())
                .orElseGet(() -> {
                    StoreCreditAccountEntity created = new StoreCreditAccountEntity();
                    created.setMerchant(merchant);
                    created.setCustomer(customer);
                    created.setBalanceAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY));
                    return created;
                });

        BigDecimal balanceBefore = account.getBalanceAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal balanceAfter = balanceBefore.add(amount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        account.setBalanceAmount(balanceAfter);

        StoreCreditTransactionEntity issueTx = new StoreCreditTransactionEntity();
        issueTx.setTransactionType(StoreCreditTransactionType.ISSUE);
        issueTx.setAmount(amount);
        issueTx.setBalanceBefore(balanceBefore);
        issueTx.setBalanceAfter(balanceAfter);
        issueTx.setSaleReturn(saleReturn);
        issueTx.setReference(normalizeOptional(request.reference()));
        issueTx.setNote(normalizeOptional(request.note()));
        account.addTransaction(issueTx);

        return toResponse(storeCreditAccountRepository.save(account));
    }

    @Transactional
    public StoreCreditAccountResponse redeem(Long accountId, StoreCreditRedeemRequest request) {
        BigDecimal amount = normalizeAmount(request.amount(), "amount");

        StoreCreditAccountEntity account = storeCreditAccountRepository
                .findByIdAndMerchantIdForUpdate(accountId, request.merchantId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store credit account not found for merchant and id"));

        SaleEntity sale = requireSale(request.saleId());
        ensureMerchantContext(account, sale);
        ensureSaleCustomerContext(account, sale);

        BigDecimal balanceBefore = account.getBalanceAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (amount.compareTo(balanceBefore) > 0) {
            throw new BaseException(ErrorCode.CONFLICT, "store credit balance cannot go below zero");
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        account.setBalanceAmount(balanceAfter);

        StoreCreditTransactionEntity redeemTx = new StoreCreditTransactionEntity();
        redeemTx.setTransactionType(StoreCreditTransactionType.REDEEM);
        redeemTx.setAmount(amount);
        redeemTx.setBalanceBefore(balanceBefore);
        redeemTx.setBalanceAfter(balanceAfter);
        redeemTx.setSale(sale);
        redeemTx.setReference(normalizeOptional(request.reference()));
        redeemTx.setNote(normalizeOptional(request.note()));
        account.addTransaction(redeemTx);

        return toResponse(storeCreditAccountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public StoreCreditAccountResponse getById(Long merchantId, Long accountId) {
        StoreCreditAccountEntity account = storeCreditAccountRepository.findByIdAndMerchantId(accountId, merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store credit account not found for merchant and id"));
        return toResponse(account);
    }

    private MerchantEntity requireMerchant(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "merchant not found: " + merchantId));
    }

    private CustomerEntity requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "customer not found: " + customerId));
    }

    private SaleReturnEntity requireSaleReturn(Long saleReturnId) {
        return saleReturnRepository.findById(saleReturnId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale return not found: " + saleReturnId));
    }

    private SaleEntity requireSale(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale not found: " + saleId));
    }

    private void validateCustomerMerchant(CustomerEntity customer, MerchantEntity merchant) {
        if (!customer.getMerchant().getId().equals(merchant.getId())) {
            throw new BaseException(ErrorCode.CONFLICT, "customer merchant does not match store credit merchant");
        }
    }

    private void validateActiveCustomer(CustomerEntity customer) {
        if (!customer.isActive()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "customer is inactive: " + customer.getId());
        }
    }

    private void ensureMerchantContext(MerchantEntity merchant, SaleReturnEntity saleReturn) {
        Long saleReturnMerchantId = saleReturn.getSale().getStoreLocation().getMerchant().getId();
        if (!merchant.getId().equals(saleReturnMerchantId)) {
            throw new BaseException(ErrorCode.CONFLICT, "sale return merchant does not match store credit merchant");
        }
    }

    private void ensureSaleReturnCustomerContext(CustomerEntity customer, SaleReturnEntity saleReturn) {
        SaleEntity sale = saleReturn.getSale();
        if (sale.getCustomer() == null) {
            throw new BaseException(ErrorCode.CONFLICT, "sale return is not linked to a customer");
        }
        if (!sale.getCustomer().getId().equals(customer.getId())) {
            throw new BaseException(ErrorCode.CONFLICT, "sale return customer does not match store credit customer");
        }
    }

    private void ensureMerchantContext(StoreCreditAccountEntity account, SaleEntity sale) {
        Long accountMerchantId = account.getMerchant().getId();
        Long saleMerchantId = sale.getStoreLocation().getMerchant().getId();
        if (!accountMerchantId.equals(saleMerchantId)) {
            throw new BaseException(ErrorCode.CONFLICT, "sale merchant does not match store credit merchant");
        }
    }

    private void ensureSaleCustomerContext(StoreCreditAccountEntity account, SaleEntity sale) {
        if (sale.getCustomer() == null) {
            throw new BaseException(ErrorCode.CONFLICT, "sale is not linked to a customer");
        }
        Long accountCustomerId = account.getCustomer().getId();
        if (!accountCustomerId.equals(sale.getCustomer().getId())) {
            throw new BaseException(ErrorCode.CONFLICT, "sale customer does not match store credit customer");
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

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private StoreCreditAccountResponse toResponse(StoreCreditAccountEntity account) {
        List<StoreCreditTransactionResponse> transactions = account.getTransactions().stream()
                .map(transaction -> new StoreCreditTransactionResponse(
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

        return new StoreCreditAccountResponse(
                account.getId(),
                account.getMerchant().getId(),
                account.getCustomer().getId(),
                account.getBalanceAmount(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                transactions);
    }
}
