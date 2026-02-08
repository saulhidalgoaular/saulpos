package com.saulpos.server.shift.service;

import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.shift.model.CashMovementEntity;
import com.saulpos.server.shift.model.CashMovementType;
import com.saulpos.server.shift.model.CashShiftEntity;
import com.saulpos.server.shift.model.CashShiftStatus;
import com.saulpos.server.shift.repository.CashMovementRepository;
import com.saulpos.server.shift.repository.CashShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final CashShiftRepository cashShiftRepository;
    private final CashMovementRepository cashMovementRepository;
    private final UserAccountRepository userAccountRepository;
    private final TerminalDeviceRepository terminalDeviceRepository;

    @Transactional
    public CashShiftResponse openShift(CashShiftOpenRequest request) {
        UserAccountEntity cashierUser = requireCashierUser(request.cashierUserId());
        TerminalDeviceEntity terminalDevice = requireTerminalDeviceForUpdate(request.terminalDeviceId());

        validateActiveTerminalHierarchy(terminalDevice);
        ensureNoOpenShift(cashierUser.getId(), terminalDevice.getId());

        CashShiftEntity cashShift = new CashShiftEntity();
        cashShift.setCashierUser(cashierUser);
        cashShift.setTerminalDevice(terminalDevice);
        cashShift.setStoreLocation(terminalDevice.getStoreLocation());
        cashShift.setStatus(CashShiftStatus.OPEN);
        cashShift.setOpeningCash(normalizeMoney(request.openingCash()));
        cashShift.setTotalPaidIn(ZERO);
        cashShift.setTotalPaidOut(ZERO);

        CashShiftEntity savedShift = cashShiftRepository.save(cashShift);
        cashMovementRepository.save(createMovement(savedShift, CashMovementType.OPEN, savedShift.getOpeningCash(), "Shift opened"));

        return toShiftResponse(savedShift);
    }

    @Transactional
    public CashMovementResponse addCashMovement(Long shiftId, CashMovementRequest request) {
        CashShiftEntity shift = requireShift(shiftId);
        ensureShiftOpen(shift);

        CashMovementType movementType = CashMovementType.valueOf(request.movementType().name());
        if (movementType != CashMovementType.PAID_IN && movementType != CashMovementType.PAID_OUT) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR,
                    "cash movement type must be PAID_IN or PAID_OUT for this endpoint");
        }

        BigDecimal amount = normalizeMoney(request.amount());
        if (amount.compareTo(ZERO) <= 0) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "cash movement amount must be greater than zero");
        }

        if (movementType == CashMovementType.PAID_IN) {
            shift.setTotalPaidIn(normalizeMoney(shift.getTotalPaidIn().add(amount)));
        } else {
            shift.setTotalPaidOut(normalizeMoney(shift.getTotalPaidOut().add(amount)));
        }

        cashShiftRepository.save(shift);

        CashMovementEntity movement = cashMovementRepository.save(
                createMovement(shift, movementType, amount, normalizeNote(request.note())));

        return toMovementResponse(movement);
    }

    @Transactional
    public CashShiftResponse closeShift(Long shiftId, CashShiftCloseRequest request) {
        CashShiftEntity shift = requireShift(shiftId);
        ensureShiftOpen(shift);

        BigDecimal countedCash = normalizeMoney(request.countedCash());
        BigDecimal expectedCash = normalizeMoney(shift.getOpeningCash()
                .add(shift.getTotalPaidIn())
                .subtract(shift.getTotalPaidOut()));
        BigDecimal varianceCash = normalizeMoney(countedCash.subtract(expectedCash));

        shift.setExpectedCloseCash(expectedCash);
        shift.setCountedCloseCash(countedCash);
        shift.setVarianceCash(varianceCash);
        shift.setClosedAt(Instant.now());
        shift.setStatus(CashShiftStatus.CLOSED);

        CashShiftEntity closedShift = cashShiftRepository.save(shift);
        cashMovementRepository.save(createMovement(
                closedShift,
                CashMovementType.CLOSE,
                countedCash,
                normalizeNote(request.note())));

        return toShiftResponse(closedShift);
    }

    @Transactional(readOnly = true)
    public CashShiftResponse getShift(Long shiftId) {
        return toShiftResponse(requireShift(shiftId));
    }

    private void ensureNoOpenShift(Long cashierUserId, Long terminalDeviceId) {
        cashShiftRepository.findByCashierUserIdAndTerminalDeviceIdAndStatus(
                        cashierUserId,
                        terminalDeviceId,
                        CashShiftStatus.OPEN)
                .ifPresent(existing -> {
                    throw new BaseException(ErrorCode.CONFLICT,
                            "an open shift already exists for cashierUserId=%d and terminalDeviceId=%d"
                                    .formatted(cashierUserId, terminalDeviceId));
                });
    }

    private void ensureShiftOpen(CashShiftEntity shift) {
        if (shift.getStatus() != CashShiftStatus.OPEN) {
            throw new BaseException(ErrorCode.CONFLICT, "shift is already closed: " + shift.getId());
        }
    }

    private void validateActiveTerminalHierarchy(TerminalDeviceEntity terminalDevice) {
        StoreLocationEntity storeLocation = terminalDevice.getStoreLocation();
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

    private CashShiftEntity requireShift(Long shiftId) {
        return cashShiftRepository.findById(shiftId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "cash shift not found: " + shiftId));
    }

    private CashMovementEntity createMovement(CashShiftEntity shift,
                                              CashMovementType movementType,
                                              BigDecimal amount,
                                              String note) {
        CashMovementEntity movement = new CashMovementEntity();
        movement.setShift(shift);
        movement.setMovementType(movementType);
        movement.setAmount(normalizeMoney(amount));
        movement.setNote(note);
        return movement;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private CashShiftResponse toShiftResponse(CashShiftEntity shift) {
        return new CashShiftResponse(
                shift.getId(),
                shift.getCashierUser().getId(),
                shift.getTerminalDevice().getId(),
                shift.getStoreLocation().getId(),
                com.saulpos.api.shift.CashShiftStatus.valueOf(shift.getStatus().name()),
                shift.getOpeningCash(),
                shift.getTotalPaidIn(),
                shift.getTotalPaidOut(),
                shift.getExpectedCloseCash(),
                shift.getCountedCloseCash(),
                shift.getVarianceCash(),
                shift.getOpenedAt(),
                shift.getClosedAt());
    }

    private CashMovementResponse toMovementResponse(CashMovementEntity movement) {
        return new CashMovementResponse(
                movement.getId(),
                movement.getShift().getId(),
                com.saulpos.api.shift.CashMovementType.valueOf(movement.getMovementType().name()),
                movement.getAmount(),
                movement.getNote(),
                movement.getOccurredAt());
    }
}
