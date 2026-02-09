package com.saulpos.api.sale;

import com.saulpos.api.tax.TenderType;

public record SaleCartRecalculateRequest(
        TenderType tenderType
) {
}
