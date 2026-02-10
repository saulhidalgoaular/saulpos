package com.saulpos.server.receipt.printing;

import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class DefaultReceiptTemplateRenderer implements ReceiptTemplateRenderer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    @Override
    public String render(ReceiptPrintPayload payload) {
        StringBuilder builder = new StringBuilder();
        builder.append(center(payload.storeName())).append('\n');
        builder.append(payload.storeCode()).append(" / ").append(payload.terminalCode()).append('\n');
        builder.append("Receipt ").append(payload.receiptNumber()).append('\n');
        builder.append("Cashier ").append(payload.cashierUsername()).append('\n');
        builder.append("Date ").append(DATE_TIME_FORMATTER.format(payload.soldAt())).append(" UTC").append('\n');
        if (payload.copy()) {
            builder.append("*** COPY ***").append('\n');
        }
        builder.append("--------------------------------").append('\n');

        for (ReceiptPrintPayload.Line line : payload.lines()) {
            builder.append(line.lineNumber())
                    .append(". ")
                    .append(trimLine(line.productName()))
                    .append('\n');
            builder.append(formatMoney(line.quantity().setScale(3, RoundingMode.HALF_UP)))
                    .append(" x ")
                    .append(formatMoney(line.unitPrice()))
                    .append(" = ")
                    .append(formatMoney(line.grossAmount()))
                    .append('\n');
        }

        builder.append("--------------------------------").append('\n');
        builder.append("Subtotal: ").append(formatMoney(payload.subtotal())).append('\n');
        builder.append("Tax:      ").append(formatMoney(payload.tax())).append('\n');
        builder.append("Total:    ").append(formatMoney(payload.total())).append('\n');
        builder.append("--------------------------------").append('\n');
        builder.append("Thank you").append('\n');
        return builder.toString();
    }

    private String trimLine(String value) {
        if (value == null || value.isBlank()) {
            return "ITEM";
        }
        String trimmed = value.trim();
        return trimmed.length() > 24 ? trimmed.substring(0, 24) : trimmed;
    }

    private String center(String value) {
        if (value == null || value.isBlank()) {
            return "STORE";
        }
        return value.trim();
    }

    private String formatMoney(java.math.BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
