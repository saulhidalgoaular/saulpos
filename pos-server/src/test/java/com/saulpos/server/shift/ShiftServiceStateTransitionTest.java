package com.saulpos.server.shift;

import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementType;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.shift.model.CashShiftEntity;
import com.saulpos.server.shift.model.CashShiftStatus;
import com.saulpos.server.shift.repository.CashMovementRepository;
import com.saulpos.server.shift.repository.CashShiftRepository;
import com.saulpos.server.shift.service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftServiceStateTransitionTest {

    @Mock
    private CashShiftRepository cashShiftRepository;

    @Mock
    private CashMovementRepository cashMovementRepository;

    @Mock
    private com.saulpos.server.security.repository.UserAccountRepository userAccountRepository;

    @Mock
    private com.saulpos.server.identity.repository.TerminalDeviceRepository terminalDeviceRepository;

    private ShiftService shiftService;

    @BeforeEach
    void setUp() {
        shiftService = new ShiftService(cashShiftRepository, cashMovementRepository, userAccountRepository, terminalDeviceRepository);
    }

    @Test
    void addCashMovementRejectsClosedShift() {
        CashShiftEntity closedShift = shiftWithStatus(CashShiftStatus.CLOSED);
        when(cashShiftRepository.findById(10L)).thenReturn(Optional.of(closedShift));

        assertThatThrownBy(() -> shiftService.addCashMovement(
                10L,
                new CashMovementRequest(CashMovementType.PAID_IN, new BigDecimal("5.00"), null)))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> assertThat(((BaseException) exception).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

        verifyNoInteractions(cashMovementRepository);
    }

    @Test
    void closeShiftRejectsClosedShift() {
        CashShiftEntity closedShift = shiftWithStatus(CashShiftStatus.CLOSED);
        when(cashShiftRepository.findById(20L)).thenReturn(Optional.of(closedShift));

        assertThatThrownBy(() -> shiftService.closeShift(
                20L,
                new CashShiftCloseRequest(new BigDecimal("10.00"), "close")))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> assertThat(((BaseException) exception).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

        verifyNoInteractions(cashMovementRepository);
    }

    @Test
    void addCashMovementRejectsOpenAndCloseMovementTypes() {
        CashShiftEntity openShift = shiftWithStatus(CashShiftStatus.OPEN);
        when(cashShiftRepository.findById(30L)).thenReturn(Optional.of(openShift));

        assertThatThrownBy(() -> shiftService.addCashMovement(
                30L,
                new CashMovementRequest(CashMovementType.OPEN, new BigDecimal("2.00"), null)))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> assertThat(((BaseException) exception).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));

        verifyNoInteractions(cashMovementRepository);
    }

    private CashShiftEntity shiftWithStatus(CashShiftStatus status) {
        CashShiftEntity shift = new CashShiftEntity();
        shift.setId(999L);
        shift.setStatus(status);
        shift.setOpeningCash(new BigDecimal("50.00"));
        shift.setTotalPaidIn(new BigDecimal("0.00"));
        shift.setTotalPaidOut(new BigDecimal("0.00"));
        return shift;
    }
}
