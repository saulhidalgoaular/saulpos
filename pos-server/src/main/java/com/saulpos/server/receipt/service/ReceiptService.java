package com.saulpos.server.receipt.service;

import com.saulpos.api.receipt.ReceiptAllocationRequest;
import com.saulpos.api.receipt.ReceiptAllocationResponse;
import com.saulpos.api.receipt.ReceiptJournalResponse;
import com.saulpos.api.receipt.ReceiptNumberPolicy;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.receipt.model.ReceiptHeaderEntity;
import com.saulpos.server.receipt.model.ReceiptSequenceEntity;
import com.saulpos.server.receipt.model.ReceiptSeriesEntity;
import com.saulpos.server.receipt.repository.ReceiptHeaderRepository;
import com.saulpos.server.receipt.repository.ReceiptSequenceRepository;
import com.saulpos.server.receipt.repository.ReceiptSeriesRepository;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final int RECEIPT_NUMBER_PADDING = 8;

    private final TerminalDeviceRepository terminalDeviceRepository;
    private final ReceiptSeriesRepository receiptSeriesRepository;
    private final ReceiptSequenceRepository receiptSequenceRepository;
    private final ReceiptHeaderRepository receiptHeaderRepository;
    private final SaleRepository saleRepository;

    private final ConcurrentMap<Long, ReentrantLock> seriesInitLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ReentrantLock> sequenceInitLocks = new ConcurrentHashMap<>();

    @Transactional
    public ReceiptAllocationResponse allocate(ReceiptAllocationRequest request) {
        TerminalDeviceEntity terminalDevice = requireTerminalDevice(request.terminalDeviceId());
        validateActiveTerminalHierarchy(terminalDevice);

        ReceiptSeriesEntity series = findOrCreateSeries(terminalDevice);
        ReceiptSequenceEntity sequence = receiptSequenceRepository.findBySeriesIdForUpdate(series.getId())
                .orElseGet(() -> createSequence(series));
        long number = sequence.getNextNumber();
        sequence.setNextNumber(number + 1);
        receiptSequenceRepository.save(sequence);

        Instant issuedAt = Instant.now();

        ReceiptHeaderEntity header = new ReceiptHeaderEntity();
        header.setSeries(series);
        header.setStoreLocation(series.getStoreLocation());
        header.setTerminalDevice(series.getTerminalDevice());
        header.setNumber(number);
        header.setReceiptNumber(formatReceiptNumber(series.getSeriesCode(), number));
        header.setIssuedAt(issuedAt);

        ReceiptHeaderEntity savedHeader = receiptHeaderRepository.save(header);

        return new ReceiptAllocationResponse(
                savedHeader.getId(),
                series.getId(),
                series.getStoreLocation().getId(),
                series.getTerminalDevice().getId(),
                series.getSeriesCode(),
                series.getNumberPolicy(),
                savedHeader.getNumber(),
                savedHeader.getReceiptNumber(),
                savedHeader.getIssuedAt());
    }

    @Transactional(readOnly = true)
    public ReceiptJournalResponse getJournalBySaleId(Long saleId) {
        SaleEntity sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "sale not found: " + saleId));
        return toJournalResponse(sale);
    }

    @Transactional(readOnly = true)
    public ReceiptJournalResponse getJournalByReceiptNumber(String receiptNumber) {
        SaleEntity sale = saleRepository.findByReceiptNumberIgnoreCase(normalizeReceiptNumber(receiptNumber))
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "sale not found for receiptNumber: " + receiptNumber));
        return toJournalResponse(sale);
    }

    private ReceiptJournalResponse toJournalResponse(SaleEntity sale) {
        return new ReceiptJournalResponse(
                sale.getId(),
                sale.getReceiptHeaderId(),
                sale.getReceiptNumber(),
                sale.getStoreLocation().getId(),
                sale.getStoreLocation().getCode(),
                sale.getTerminalDevice().getId(),
                sale.getTerminalDevice().getCode(),
                sale.getCashierUser().getId(),
                sale.getCashierUser().getUsername(),
                sale.getTotalPayable(),
                sale.getCreatedAt());
    }

    private ReceiptSeriesEntity findOrCreateSeries(TerminalDeviceEntity terminalDevice) {
        ReceiptSeriesEntity existingSeries = receiptSeriesRepository.findByTerminalDeviceId(terminalDevice.getId())
                .map(this::validateActiveSeries)
                .orElse(null);
        if (existingSeries != null) {
            return existingSeries;
        }

        ReentrantLock lock = seriesInitLocks.computeIfAbsent(terminalDevice.getId(), ignored -> new ReentrantLock());
        lock.lock();
        try {
            return receiptSeriesRepository.findByTerminalDeviceId(terminalDevice.getId())
                    .map(this::validateActiveSeries)
                    .orElseGet(() -> createSeries(terminalDevice));
        } finally {
            lock.unlock();
        }
    }

    private ReceiptSequenceEntity createInitialSequence(ReceiptSeriesEntity series) {
        ReceiptSequenceEntity sequence = new ReceiptSequenceEntity();
        sequence.setSeries(series);
        sequence.setNextNumber(1L);
        return sequence;
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

    private TerminalDeviceEntity requireTerminalDevice(Long terminalDeviceId) {
        return terminalDeviceRepository.findById(terminalDeviceId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "terminal device not found: " + terminalDeviceId));
    }

    private ReceiptSeriesEntity validateActiveSeries(ReceiptSeriesEntity series) {
        if (!series.isActive()) {
            throw new BaseException(ErrorCode.CONFLICT,
                    "receipt series is inactive for terminal: " + series.getTerminalDevice().getId());
        }
        return series;
    }

    private ReceiptSeriesEntity createSeries(TerminalDeviceEntity terminalDevice) {
        ReceiptSeriesEntity series = new ReceiptSeriesEntity();
        series.setStoreLocation(terminalDevice.getStoreLocation());
        series.setTerminalDevice(terminalDevice);
        series.setSeriesCode(buildSeriesCode(terminalDevice));
        series.setNumberPolicy(ReceiptNumberPolicy.GAPLESS);
        series.setActive(true);

        ReceiptSeriesEntity savedSeries = receiptSeriesRepository.save(series);
        createSequence(savedSeries);
        return savedSeries;
    }

    private ReceiptSequenceEntity createSequence(ReceiptSeriesEntity series) {
        ReentrantLock lock = sequenceInitLocks.computeIfAbsent(series.getId(), ignored -> new ReentrantLock());
        lock.lock();
        try {
            return receiptSequenceRepository.findById(series.getId())
                    .orElseGet(() -> receiptSequenceRepository.saveAndFlush(createInitialSequence(series)));
        } finally {
            lock.unlock();
        }
    }

    private String buildSeriesCode(TerminalDeviceEntity terminalDevice) {
        String base = terminalDevice.getCode() == null
                ? "TERMINAL-" + terminalDevice.getId()
                : terminalDevice.getCode().trim().toUpperCase(Locale.ROOT);

        base = base.replaceAll("[^A-Z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "TERMINAL-" + terminalDevice.getId();
        }

        String seriesCode = "RCPT-" + base;
        if (seriesCode.length() > 40) {
            return seriesCode.substring(0, 40);
        }
        return seriesCode;
    }

    private String formatReceiptNumber(String seriesCode, long number) {
        return ("%s-%0" + RECEIPT_NUMBER_PADDING + "d").formatted(seriesCode, number);
    }

    private String normalizeReceiptNumber(String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "receiptNumber is required");
        }
        return receiptNumber.trim();
    }
}
