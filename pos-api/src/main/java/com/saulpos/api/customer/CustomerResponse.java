package com.saulpos.api.customer;

import java.util.List;

public record CustomerResponse(
        Long id,
        Long merchantId,
        String displayName,
        boolean invoiceRequired,
        boolean creditEnabled,
        boolean active,
        List<CustomerTaxIdentityResponse> taxIdentities,
        List<CustomerContactResponse> contacts
) {
}
