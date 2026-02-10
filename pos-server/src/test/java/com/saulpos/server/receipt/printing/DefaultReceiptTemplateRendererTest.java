package com.saulpos.server.receipt.printing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultReceiptTemplateRendererTest {

    private final DefaultReceiptTemplateRenderer renderer = new DefaultReceiptTemplateRenderer();

    @Test
    void renderIncludesHeaderLinesTotalsAndCopyMarker() {
        ReceiptPrintPayload payload = new ReceiptPrintPayload(
                "RCPT-TERM-00000001",
                "STORE-01",
                "Main Store",
                "TERM-01",
                "cashier",
                Instant.parse("2026-02-10T12:00:00Z"),
                List.of(new ReceiptPrintPayload.Line(
                        1,
                        "Sparkling Water 500ml",
                        new BigDecimal("2.000"),
                        new BigDecimal("1.50"),
                        new BigDecimal("3.00"))),
                new BigDecimal("2.70"),
                new BigDecimal("0.30"),
                new BigDecimal("3.00"),
                true);

        String rendered = renderer.render(payload);

        assertThat(rendered).contains("Main Store");
        assertThat(rendered).contains("Receipt RCPT-TERM-00000001");
        assertThat(rendered).contains("*** COPY ***");
        assertThat(rendered).contains("Subtotal: 2.70");
        assertThat(rendered).contains("Tax:      0.30");
        assertThat(rendered).contains("Total:    3.00");
    }
}
