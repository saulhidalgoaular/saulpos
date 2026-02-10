package com.saulpos.server.receipt.service;

import com.saulpos.api.receipt.CashDrawerOpenRequest;
import com.saulpos.api.receipt.CashDrawerOpenResponse;
import com.saulpos.api.receipt.CashDrawerOpenStatus;
import com.saulpos.core.printing.PrintJob;
import com.saulpos.core.printing.PrintResult;
import com.saulpos.core.printing.PrintStatus;
import com.saulpos.core.printing.PrinterAdapter;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.receipt.model.NoSaleDrawerEventEntity;
import com.saulpos.server.receipt.repository.NoSaleDrawerEventRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CashDrawerService {

    private static final String ADAPTER_NAME = "ESC_POS";
    private static final byte[] DRAWER_OPEN_PULSE = new byte[] {0x1B, 0x70, 0x00, 0x3C, (byte) 0xFF};

    private final TerminalDeviceRepository terminalDeviceRepository;
    private final UserAccountRepository userAccountRepository;
    private final NoSaleDrawerEventRepository noSaleDrawerEventRepository;
    private final PrinterAdapter printerAdapter;

    @Transactional
    public CashDrawerOpenResponse open(CashDrawerOpenRequest request) {
        TerminalDeviceEntity terminal = terminalDeviceRepository.findById(request.terminalDeviceId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "terminalDevice not found for id: " + request.terminalDeviceId()));

        UserAccountEntity actorUser = resolveActorUser();
        String actorUsername = resolveActorUsername();

        PrintResult result;
        try {
            result = printerAdapter.print(new PrintJob(
                    "drawer-open-" + terminal.getId() + "-" + Instant.now().toEpochMilli(),
                    terminal.getCode(),
                    DRAWER_OPEN_PULSE));
        } catch (RuntimeException exception) {
            result = PrintResult.failed(true, "drawer open dispatch failed: " + exception.getMessage());
        }
        if (result == null) {
            result = PrintResult.failed(true, "drawer adapter returned no result");
        }

        NoSaleDrawerEventEntity event = new NoSaleDrawerEventEntity();
        event.setStoreLocation(terminal.getStoreLocation());
        event.setTerminalDevice(terminal);
        event.setCashierUser(actorUser);
        event.setActorUser(actorUser);
        event.setActorUsername(actorUsername);
        event.setReasonCode(normalizeReasonCode(request.reasonCode()));
        event.setNote(normalizeOptionalText(request.note()));
        event.setReferenceNumber(normalizeOptionalText(request.referenceNumber()));
        event.setCorrelationId(MDC.get("correlationId"));
        event = noSaleDrawerEventRepository.save(event);

        return new CashDrawerOpenResponse(
                event.getId(),
                terminal.getId(),
                terminal.getCode(),
                mapStatus(result.status()),
                ADAPTER_NAME,
                result.retryable(),
                result.message(),
                event.getCreatedAt());
    }

    private CashDrawerOpenStatus mapStatus(PrintStatus status) {
        return status == PrintStatus.SUCCESS ? CashDrawerOpenStatus.SUCCESS : CashDrawerOpenStatus.FAILED;
    }

    private UserAccountEntity resolveActorUser() {
        String username = resolveActorUsername();
        return userAccountRepository.findByUsernameIgnoreCase(username).orElse(null);
    }

    private String resolveActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "unknown";
        }
        return authentication.getName().trim();
    }

    private String normalizeReasonCode(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "reasonCode is required");
        }
        return reasonCode.trim().toUpperCase();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
