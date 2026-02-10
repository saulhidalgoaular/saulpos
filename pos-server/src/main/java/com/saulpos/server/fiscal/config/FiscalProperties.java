package com.saulpos.server.fiscal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.fiscal")
public class FiscalProperties {

    private boolean enabled = false;
    private boolean allowInvoiceWithDisabledProvider = true;
}
