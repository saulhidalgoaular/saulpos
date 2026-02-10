package com.saulpos.server.fiscal.config;

import com.saulpos.core.fiscal.FiscalProvider;

/**
 * Country module hook for registering fiscal providers without changing core sales contracts.
 */
public interface CountryFiscalProviderFactory {

    String countryCode();

    FiscalProvider createProvider();
}
