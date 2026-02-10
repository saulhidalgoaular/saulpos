package com.saulpos.server.receipt.printing;

import com.saulpos.core.printing.PrintJob;
import com.saulpos.core.printing.PrintResult;
import com.saulpos.core.printing.PrintStatus;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class EscPosPrinterAdapterTest {

    private final EscPosPrinterAdapter adapter = new EscPosPrinterAdapter();

    @Test
    void printReturnsFailureForMissingPayload() {
        PrintResult result = adapter.print(new PrintJob("job-1", "TERM-1", new byte[0]));

        assertThat(result.status()).isEqualTo(PrintStatus.FAILED);
        assertThat(result.retryable()).isFalse();
        assertThat(result.message()).isEqualTo("print payload is empty");
    }

    @Test
    void printReturnsFailureForMissingTarget() {
        PrintResult result = adapter.print(new PrintJob("job-2", "   ", "receipt".getBytes(StandardCharsets.UTF_8)));

        assertThat(result.status()).isEqualTo(PrintStatus.FAILED);
        assertThat(result.retryable()).isFalse();
        assertThat(result.message()).isEqualTo("printer target is required");
    }

    @Test
    void printReturnsSuccessForValidJob() {
        PrintResult result = adapter.print(new PrintJob("job-3", "TERM-1", "receipt".getBytes(StandardCharsets.UTF_8)));

        assertThat(result.status()).isEqualTo(PrintStatus.SUCCESS);
        assertThat(result.retryable()).isFalse();
        assertThat(result.message()).contains("print dispatched to TERM-1");
    }
}
