package com.saulpos.server.system.service;

import com.saulpos.api.system.OfflineMode;
import com.saulpos.api.system.OfflineOperationPolicyResponse;
import com.saulpos.api.system.OfflinePolicyResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfflinePolicyService {

    public OfflinePolicyResponse currentPolicy() {
        return new OfflinePolicyResponse(
                "K1-v1",
                "Server connectivity is required for transactional flows in SaulPOS v2.",
                List.of(
                        new OfflineOperationPolicyResponse(
                                "AUTH_LOGIN",
                                OfflineMode.ONLINE_ONLY,
                                "Client blocks login submission while server is unreachable.",
                                "Cannot sign in while offline. Reconnect to continue."),
                        new OfflineOperationPolicyResponse(
                                "CART_MUTATION",
                                OfflineMode.ONLINE_ONLY,
                                "Client disables add/update/remove line actions while disconnected.",
                                "Cart changes are unavailable offline. Reconnect and try again."),
                        new OfflineOperationPolicyResponse(
                                "CHECKOUT",
                                OfflineMode.ONLINE_ONLY,
                                "Checkout action is blocked unless live server round-trip is available.",
                                "Sale cannot be completed offline. Reconnect to finalize payment."),
                        new OfflineOperationPolicyResponse(
                                "CATALOG_REFERENCE_VIEW",
                                OfflineMode.DEGRADED_READ_ONLY,
                                "Client may keep last-synced catalog view but must block transactional actions.",
                                "You can view cached catalog data, but sales actions stay disabled until reconnect.")
                ));
    }
}
