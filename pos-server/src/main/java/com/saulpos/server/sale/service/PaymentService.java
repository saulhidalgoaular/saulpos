package com.saulpos.server.sale.service;

import com.saulpos.api.sale.PaymentDetailsResponse;
import com.saulpos.api.sale.PaymentStatus;
import com.saulpos.api.sale.PaymentTransitionAction;
import com.saulpos.api.sale.PaymentTransitionRequest;
import com.saulpos.api.sale.PaymentTransitionResponse;
import com.saulpos.api.sale.SaleCheckoutPaymentResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.idempotency.service.IdempotencyService;
import com.saulpos.server.sale.model.PaymentAllocationEntity;
import com.saulpos.server.sale.model.PaymentEntity;
import com.saulpos.server.sale.model.PaymentTransitionEntity;
import com.saulpos.server.sale.repository.PaymentRepository;
import com.saulpos.server.sale.repository.PaymentTransitionRepository;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransitionRepository paymentTransitionRepository;
    private final SaleRepository saleRepository;
    private final UserAccountRepository userAccountRepository;
    private final IdempotencyService idempotencyService;

    @Transactional(readOnly = true)
    public PaymentDetailsResponse getPayment(Long paymentId) {
        PaymentEntity payment = requirePaymentWithAllocations(paymentId);
        return toPaymentDetailsResponse(payment);
    }

    @Transactional
    public PaymentDetailsResponse capture(Long paymentId, String idempotencyKey, PaymentTransitionRequest request) {
        return idempotencyService.execute(
                "POST:/api/payments/%d/capture".formatted(paymentId),
                idempotencyKey,
                request,
                PaymentDetailsResponse.class,
                () -> transition(paymentId, PaymentTransitionAction.CAPTURE, request));
    }

    @Transactional
    public PaymentDetailsResponse voidPayment(Long paymentId, String idempotencyKey, PaymentTransitionRequest request) {
        return idempotencyService.execute(
                "POST:/api/payments/%d/void".formatted(paymentId),
                idempotencyKey,
                request,
                PaymentDetailsResponse.class,
                () -> transition(paymentId, PaymentTransitionAction.VOID, request));
    }

    @Transactional
    public PaymentDetailsResponse refund(Long paymentId, String idempotencyKey, PaymentTransitionRequest request) {
        return idempotencyService.execute(
                "POST:/api/payments/%d/refund".formatted(paymentId),
                idempotencyKey,
                request,
                PaymentDetailsResponse.class,
                () -> transition(paymentId, PaymentTransitionAction.REFUND, request));
    }

    @Transactional
    public void recordInitialAuthorization(PaymentEntity payment) {
        PaymentTransitionEntity transition = new PaymentTransitionEntity();
        transition.setPayment(payment);
        transition.setAction(PaymentTransitionAction.AUTHORIZE);
        transition.setFromStatus(null);
        transition.setToStatus(PaymentStatus.AUTHORIZED);
        transition.setActorUsername(resolveActorUsername());
        transition.setCorrelationId(MDC.get("correlationId"));
        transition.setNote("payment authorized at checkout");
        paymentTransitionRepository.save(transition);
    }

    private PaymentDetailsResponse transition(Long paymentId,
                                              PaymentTransitionAction action,
                                              PaymentTransitionRequest request) {
        PaymentEntity payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "payment not found: " + paymentId));

        PaymentStatus fromStatus = payment.getStatus();
        PaymentStatus toStatus = PaymentStateMachine.nextStatus(fromStatus, action);
        if (toStatus == null) {
            throw new BaseException(
                    ErrorCode.CONFLICT,
                    "invalid payment transition from %s with action %s".formatted(fromStatus, action));
        }

        payment.setStatus(toStatus);
        paymentRepository.save(payment);

        String actorUsername = resolveActorUsername();
        Optional<UserAccountEntity> actorUser = userAccountRepository.findByUsernameIgnoreCase(actorUsername);

        PaymentTransitionEntity transition = new PaymentTransitionEntity();
        transition.setPayment(payment);
        transition.setAction(action);
        transition.setFromStatus(fromStatus);
        transition.setToStatus(toStatus);
        transition.setActorUser(actorUser.orElse(null));
        transition.setActorUsername(actorUsername);
        transition.setNote(normalizeNote(request == null ? null : request.note()));
        transition.setCorrelationId(MDC.get("correlationId"));
        paymentTransitionRepository.save(transition);

        return toPaymentDetailsResponse(requirePaymentWithAllocations(paymentId));
    }

    private PaymentDetailsResponse toPaymentDetailsResponse(PaymentEntity payment) {
        Long saleId = saleRepository.findByCartId(payment.getCart().getId())
                .map(sale -> sale.getId())
                .orElse(null);

        List<PaymentTransitionResponse> transitions = paymentTransitionRepository
                .findByPaymentIdOrderByCreatedAtAscIdAsc(payment.getId())
                .stream()
                .map(this::toTransitionResponse)
                .toList();

        return new PaymentDetailsResponse(
                payment.getId(),
                payment.getCart().getId(),
                saleId,
                payment.getStatus(),
                payment.getTotalPayable(),
                payment.getTotalAllocated(),
                payment.getTotalTendered(),
                payment.getChangeAmount(),
                toPaymentResponses(payment),
                transitions,
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }

    private PaymentTransitionResponse toTransitionResponse(PaymentTransitionEntity transition) {
        return new PaymentTransitionResponse(
                transition.getAction(),
                transition.getFromStatus(),
                transition.getToStatus(),
                transition.getActorUsername(),
                transition.getNote(),
                transition.getCorrelationId(),
                transition.getCreatedAt());
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

    private PaymentEntity requirePaymentWithAllocations(Long paymentId) {
        return paymentRepository.findByIdWithAllocations(paymentId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "payment not found: " + paymentId));
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "unknown";
        }
        return authentication.getName();
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
