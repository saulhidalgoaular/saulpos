package com.saulpos.api.system;

public record OfflineOperationPolicyResponse(
        String operation,
        OfflineMode mode,
        String technicalControl,
        String userMessage
) {
}
