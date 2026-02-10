package com.saulpos.server.sale.service;

import com.saulpos.api.receipt.ReceiptAllocationRequest;
import com.saulpos.api.receipt.ReceiptAllocationResponse;
import com.saulpos.api.sale.PaymentStatus;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.sale.SaleCheckoutPaymentResponse;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.inventory.service.InventoryLotService;
import com.saulpos.server.receipt.service.ReceiptService;
import com.saulpos.server.sale.model.InventoryMovementEntity;
import com.saulpos.server.sale.model.InventoryReferenceType;
import com.saulpos.server.sale.model.InventoryMovementType;
import com.saulpos.server.sale.model.PaymentAllocationEntity;
import com.saulpos.server.sale.model.PaymentEntity;
import com.saulpos.server.sale.model.SaleCartEntity;
import com.saulpos.server.sale.model.SaleCartLineEntity;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleLineEntity;
import com.saulpos.server.sale.repository.InventoryMovementRepository;
import com.saulpos.server.sale.repository.PaymentRepository;
import com.saulpos.server.sale.repository.SaleCartRepository;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.authorization.SecurityAuthority;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SaleRepository saleRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final PaymentAllocationValidator paymentAllocationValidator;
    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final InventoryLotService inventoryLotService;

    @Value("${app.inventory.expiry-override-enabled:false}")
    private boolean expiryOverrideEnabled;

    @Transactional
    public SaleCheckoutResponse checkout(SaleCheckoutRequest request) {
        SaleCartEntity cart = requireActiveCartForUpdate(request.cartId());
        requireOperatorContext(cart, request.cashierUserId(), request.terminalDeviceId());
        requireCartHasLines(cart);
        requireNoExistingSaleForCart(cart);

        PaymentAllocationValidator.ValidationResult validationResult = paymentAllocationValidator.validate(
                cart.getTotalPayable(),
                request.payments());
        CustomerEntity customer = resolveCustomerForCheckout(
                request.customerId(),
                cart.getStoreLocation().getMerchant().getId());

        ReceiptAllocationResponse receipt = receiptService.allocate(new ReceiptAllocationRequest(cart.getTerminalDevice().getId()));

        SaleEntity sale = createSale(cart, receipt, customer);
        SaleEntity savedSale = saleRepository.save(sale);
        List<InventoryMovementDraft> movementDrafts = createInventoryMovements(savedSale);
        inventoryMovementRepository.saveAll(movementDrafts.stream()
                .map(InventoryMovementDraft::movement)
                .toList());
        for (InventoryMovementDraft movementDraft : movementDrafts) {
            inventoryLotService.persistMovementLotAllocations(
                    movementDraft.movement(),
                    movementDraft.lotAllocations());
        }

        PaymentEntity savedPayment = upsertPayment(cart, validationResult);

        cart.setStatus(SaleCartStatus.CHECKED_OUT);
        saleCartRepository.save(cart);

        return new SaleCheckoutResponse(
                cart.getId(),
                savedSale.getId(),
                savedSale.getReceiptNumber(),
                savedPayment.getId(),
                savedPayment.getStatus(),
                savedPayment.getTotalPayable(),
                savedPayment.getTotalAllocated(),
                savedPayment.getTotalTendered(),
                savedPayment.getChangeAmount(),
                toPaymentResponses(savedPayment),
                savedPayment.getUpdatedAt());
    }

    private SaleEntity createSale(SaleCartEntity cart, ReceiptAllocationResponse receipt, CustomerEntity customer) {
        SaleEntity sale = new SaleEntity();
        sale.setCart(cart);
        sale.setCashierUser(cart.getCashierUser());
        sale.setStoreLocation(cart.getStoreLocation());
        sale.setTerminalDevice(cart.getTerminalDevice());
        sale.setCustomer(customer);
        sale.setReceiptHeaderId(receipt.receiptHeaderId());
        sale.setReceiptNumber(receipt.receiptNumber());
        sale.setSubtotalNet(cart.getSubtotalNet());
        sale.setTotalTax(cart.getTotalTax());
        sale.setTotalGross(cart.getTotalGross());
        sale.setRoundingAdjustment(cart.getRoundingAdjustment());
        sale.setTotalPayable(cart.getTotalPayable());

        for (SaleCartLineEntity cartLine : orderedCartLines(cart)) {
            SaleLineEntity line = new SaleLineEntity();
            line.setLineNumber(cartLine.getLineNumber());
            line.setProduct(cartLine.getProduct());
            line.setQuantity(cartLine.getQuantity());
            line.setUnitPrice(cartLine.getUnitPrice());
            line.setNetAmount(cartLine.getNetAmount());
            line.setTaxAmount(cartLine.getTaxAmount());
            line.setGrossAmount(cartLine.getGrossAmount());
            line.setOpenPriceReason(cartLine.getOpenPriceReason());
            sale.addLine(line);
        }
        return sale;
    }

    private List<InventoryMovementDraft> createInventoryMovements(SaleEntity sale) {
        boolean allowExpiredOverride = expiryOverrideEnabled
                && currentUserHasPermission(PermissionCodes.CONFIGURATION_MANAGE);

        return sale.getLines().stream()
                .map(line -> {
                    InventoryMovementEntity movement = new InventoryMovementEntity();
                    movement.setStoreLocation(sale.getStoreLocation());
                    movement.setProduct(line.getProduct());
                    movement.setSale(sale);
                    movement.setSaleLine(line);
                    movement.setMovementType(InventoryMovementType.SALE);
                    movement.setQuantityDelta(line.getQuantity().negate());
                    movement.setReferenceType(InventoryReferenceType.SALE_RECEIPT);
                    movement.setReferenceNumber(sale.getReceiptNumber());

                    List<InventoryLotService.LotAllocation> lotAllocations = inventoryLotService.allocateSaleLots(
                            sale.getStoreLocation(),
                            line.getProduct(),
                            line.getQuantity(),
                            allowExpiredOverride);

                    return new InventoryMovementDraft(movement, lotAllocations);
                })
                .toList();
    }

    private boolean currentUserHasPermission(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        String requiredAuthority = SecurityAuthority.permission(permissionCode);
        return authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .anyMatch(requiredAuthority::equalsIgnoreCase);
    }

    private PaymentEntity upsertPayment(SaleCartEntity cart, PaymentAllocationValidator.ValidationResult validationResult) {
        PaymentEntity payment = paymentRepository.findByCartIdWithAllocations(cart.getId())
                .orElseGet(PaymentEntity::new);
        payment.setCart(cart);
        payment.setTotalPayable(validationResult.totalPayable());
        payment.setTotalAllocated(validationResult.totalAllocated());
        payment.setTotalTendered(validationResult.totalTendered());
        payment.setChangeAmount(validationResult.changeAmount());
        payment.setStatus(PaymentStatus.AUTHORIZED);
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
        paymentService.recordInitialAuthorization(savedPayment);
        return savedPayment;
    }

    private List<SaleCheckoutPaymentResponse> toPaymentResponses(PaymentEntity payment) {
        return payment.getAllocations().stream()
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
    }

    private List<SaleCartLineEntity> orderedCartLines(SaleCartEntity cart) {
        return cart.getLines().stream()
                .sorted(Comparator.comparingInt(SaleCartLineEntity::getLineNumber)
                        .thenComparing(SaleCartLineEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
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

    private void requireCartHasLines(SaleCartEntity cart) {
        if (cart.getLines() == null || cart.getLines().isEmpty()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "sale cart must contain at least one line before checkout");
        }
    }

    private void requireNoExistingSaleForCart(SaleCartEntity cart) {
        if (saleRepository.findByCartId(cart.getId()).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "sale already exists for cart: " + cart.getId());
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

    private CustomerEntity resolveCustomerForCheckout(Long customerId, Long merchantId) {
        if (customerId == null) {
            return null;
        }

        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "customer not found: " + customerId));
        if (!customer.getMerchant().getId().equals(merchantId)) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "customer merchant mismatch for checkout: customerId=%d merchantId=%d"
                            .formatted(customerId, merchantId));
        }
        if (!customer.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT, "customer is inactive: " + customerId);
        }
        return customer;
    }

    private record InventoryMovementDraft(InventoryMovementEntity movement,
                                          List<InventoryLotService.LotAllocation> lotAllocations) {
    }
}
