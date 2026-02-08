package com.saulpos.api.customer;

import java.util.List;

public record CustomerResponse(
        Long id,
        Long merchantId,
        String displayName,
        boolean invoiceRequired,
        boolean creditEnabled,
        boolean active,
        List<CustomerGroupResponse> groups,
        List<CustomerTaxIdentityResponse> taxIdentities,
        List<CustomerContactResponse> contacts
) {
}
