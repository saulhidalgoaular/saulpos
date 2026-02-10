package com.saulpos.core.printing;

public record PrintJob(
        String jobId,
        String target,
        byte[] payload
) {
}
