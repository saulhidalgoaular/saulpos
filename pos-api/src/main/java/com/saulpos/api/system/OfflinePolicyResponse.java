package com.saulpos.api.system;

import java.util.List;

public record OfflinePolicyResponse(
        String policyVersion,
        String connectivityExpectation,
        List<OfflineOperationPolicyResponse> operations
) {
}
