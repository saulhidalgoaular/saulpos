package com.saulpos.server.receipt.printing;

import com.saulpos.core.printing.PrintJob;
import com.saulpos.core.printing.PrintResult;
import com.saulpos.core.printing.PrinterAdapter;
import org.springframework.stereotype.Component;

@Component
public class EscPosPrinterAdapter implements PrinterAdapter {

    private static final byte ESC = 0x1B;
    private static final byte INIT = 0x40;
    private static final byte CUT = 0x56;

    @Override
    public PrintResult print(PrintJob printJob) {
        if (printJob == null || printJob.payload() == null || printJob.payload().length == 0) {
            return PrintResult.failed(false, "print payload is empty");
        }
        if (printJob.target() == null || printJob.target().isBlank()) {
            return PrintResult.failed(false, "printer target is required");
        }

        try {
            encodeEscPos(printJob.payload());
            return PrintResult.success("print dispatched to " + printJob.target().trim());
        } catch (RuntimeException exception) {
            return PrintResult.failed(true, "printer adapter failed: " + exception.getMessage());
        }
    }

    private byte[] encodeEscPos(byte[] payload) {
        byte[] encoded = new byte[payload.length + 4];
        encoded[0] = ESC;
        encoded[1] = INIT;
        System.arraycopy(payload, 0, encoded, 2, payload.length);
        encoded[encoded.length - 2] = ESC;
        encoded[encoded.length - 1] = CUT;
        return encoded;
    }
}
