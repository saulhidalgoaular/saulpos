package com.saulpos.server.sale.service;

import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCheckoutPaymentResponse;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.sale.model.PaymentAllocationEntity;
import com.saulpos.server.sale.model.PaymentEntity;
import com.saulpos.server.sale.model.SaleCartEntity;
import com.saulpos.server.sale.repository.PaymentRepository;
import com.saulpos.server.sale.repository.SaleCartRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleCheckoutService {

    private final SaleCartRepository saleCartRepository;
    private final UserAccountRepository userAccountRepository;
    private final TerminalDeviceRepository terminalDeviceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationValidator paymentAllocationValidator;

    @Transactional
    public SaleCheckoutResponse checkout(SaleCheckoutRequest request) {
        SaleCartEntity cart = requireActiveCartForUpdate(request.cartId());
        requireOperatorContext(cart, request.cashierUserId(), request.terminalDeviceId());

        PaymentAllocationValidator.ValidationResult validationResult = paymentAllocationValidator.validate(
                cart.getTotalPayable(),
                request.payments());

        PaymentEntity payment = paymentRepository.findByCartIdWithAllocations(cart.getId())
                .orElseGet(PaymentEntity::new);
        payment.setCart(cart);
        payment.setTotalPayable(validationResult.totalPayable());
        payment.setTotalAllocated(validationResult.totalAllocated());
        payment.setTotalTendered(validationResult.totalTendered());
        payment.setChangeAmount(validationResult.changeAmount());
        payment.getAllocations().clear();

        for (PaymentAllocationValidator.ValidatedPayment validatedPayment : validationResult.payments()) {
            PaymentAllocationEntity allocation = new PaymentAllocationEntity();
            allocation.setSequenceNumber(validatedPayment.sequenceNumber());
            allocation.setTenderType(validatedPayment.tenderType());
            allocation.setAllocatedAmount(validatedPayment.amount());
            allocation.setTenderedAmount(validatedPayment.tenderedAmount());
            allocation.setChangeAmount(validatedPayment.changeAmount());
            allocation.setReference(validatedPayment.reference());
            payment.addAllocation(allocation);
        }

        PaymentEntity savedPayment = paymentRepository.save(payment);
        List<SaleCheckoutPaymentResponse> paymentResponses = savedPayment.getAllocations().stream()
                .sorted(Comparator.comparingInt(PaymentAllocationEntity::getSequenceNumber)
                        .thenComparing(PaymentAllocationEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(allocation -> new SaleCheckoutPaymentResponse(
                        allocation.getSequenceNumber(),
                        allocation.getTenderType(),
                        allocation.getAllocatedAmount(),
                        allocation.getTenderedAmount(),
                        allocation.getChangeAmount(),
                        allocation.getReference()))
                .toList();

        return new SaleCheckoutResponse(
                cart.getId(),
                savedPayment.getId(),
                savedPayment.getTotalPayable(),
                savedPayment.getTotalAllocated(),
                savedPayment.getTotalTendered(),
                savedPayment.getChangeAmount(),
                paymentResponses,
                savedPayment.getUpdatedAt());
    }

    private SaleCartEntity requireActiveCartForUpdate(Long cartId) {
        SaleCartEntity cart = saleCartRepository.findByIdForUpdate(cartId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "sale cart not found: " + cartId));
        if (cart.getStatus() != SaleCartStatus.ACTIVE) {
            throw new BaseException(ErrorCode.CONFLICT, "sale cart is not active: " + cartId);
        }
        return cart;
    }

    private UserAccountEntity requireCashierUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "cashier user not found: " + userId));
    }

    private TerminalDeviceEntity requireTerminalDeviceForUpdate(Long terminalDeviceId) {
        return terminalDeviceRepository.findByIdForUpdate(terminalDeviceId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "terminal device not found: " + terminalDeviceId));
    }

    private void requireOperatorContext(SaleCartEntity cart, Long cashierUserId, Long terminalDeviceId) {
        UserAccountEntity cashierUser = requireCashierUser(cashierUserId);
        TerminalDeviceEntity terminalDevice = requireTerminalDeviceForUpdate(terminalDeviceId);
        validateActiveHierarchy(cashierUser, terminalDevice);

        if (!cashierUser.getId().equals(cart.getCashierUser().getId())) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "cart can only be handled by the assigned cashier user: " + cart.getCashierUser().getId());
        }

        if (!terminalDevice.getId().equals(cart.getTerminalDevice().getId())) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "cart can only be handled by the assigned terminal device: " + cart.getTerminalDevice().getId());
        }

        if (!terminalDevice.getStoreLocation().getId().equals(cart.getStoreLocation().getId())) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "terminal does not belong to cart store location");
        }
    }

    private void validateActiveHierarchy(UserAccountEntity cashierUser, TerminalDeviceEntity terminalDevice) {
        StoreLocationEntity storeLocation = terminalDevice.getStoreLocation();
        if (!cashierUser.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "cashier user is inactive: " + cashierUser.getId());
        }
        if (!terminalDevice.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "terminal device is inactive: " + terminalDevice.getId());
        }
        if (!storeLocation.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "store location is inactive: " + storeLocation.getId());
        }
        if (!storeLocation.getMerchant().isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "merchant is inactive: " + storeLocation.getMerchant().getId());
        }
    }
}
