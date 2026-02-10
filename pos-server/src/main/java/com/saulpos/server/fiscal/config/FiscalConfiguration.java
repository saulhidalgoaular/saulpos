package com.saulpos.server.fiscal.config;

import com.saulpos.core.fiscal.FiscalProvider;
import com.saulpos.server.fiscal.service.StubFiscalProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(FiscalProperties.class)
public class FiscalConfiguration {

    @Bean
    @ConditionalOnMissingBean(FiscalProvider.class)
    public FiscalProvider fiscalProvider(
            FiscalProperties fiscalProperties,
            List<CountryFiscalProviderFactory> countryFiscalProviderFactories) {
        String configuredCountryCode = normalizeCountryCode(fiscalProperties.getCountryCode());
        if (configuredCountryCode == null) {
            return new StubFiscalProvider();
        }

        Map<String, CountryFiscalProviderFactory> factoriesByCountryCode = new HashMap<>();
        for (CountryFiscalProviderFactory factory : countryFiscalProviderFactories) {
            String countryCode = normalizeCountryCode(factory.countryCode());
            if (countryCode == null) {
                continue;
            }
            CountryFiscalProviderFactory existing = factoriesByCountryCode.putIfAbsent(countryCode, factory);
            if (existing != null) {
                throw new IllegalStateException("duplicate fiscal provider factory for country code: " + countryCode);
            }
        }

        CountryFiscalProviderFactory providerFactory = factoriesByCountryCode.get(configuredCountryCode);
        if (providerFactory == null) {
            throw new IllegalStateException(
                    "no fiscal provider factory configured for app.fiscal.country-code="
                            + configuredCountryCode);
        }
        return providerFactory.createProvider();
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        String normalized = countryCode.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

}
