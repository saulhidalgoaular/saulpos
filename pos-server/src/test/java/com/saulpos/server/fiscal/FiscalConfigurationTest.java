package com.saulpos.server.fiscal;

import com.saulpos.core.fiscal.FiscalCancelInvoiceCommand;
import com.saulpos.core.fiscal.FiscalIssueCreditNoteCommand;
import com.saulpos.core.fiscal.FiscalIssueInvoiceCommand;
import com.saulpos.core.fiscal.FiscalProvider;
import com.saulpos.core.fiscal.FiscalProviderResult;
import com.saulpos.server.fiscal.config.CountryFiscalProviderFactory;
import com.saulpos.server.fiscal.config.FiscalConfiguration;
import com.saulpos.server.fiscal.config.FiscalProperties;
import com.saulpos.server.fiscal.service.StubFiscalProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiscalConfigurationTest {

    private final FiscalConfiguration configuration = new FiscalConfiguration();

    @Test
    void returnsStubProviderWhenCountryCodeIsNotConfigured() {
        FiscalProperties properties = new FiscalProperties();

        FiscalProvider provider = configuration.fiscalProvider(properties, List.of());

        assertThat(provider).isInstanceOf(StubFiscalProvider.class);
        assertThat(provider.providerCode()).isEqualTo("STUB");
    }

    @Test
    void selectsCountryProviderFactoryWhenCountryCodeMatches() {
        FiscalProperties properties = new FiscalProperties();
        properties.setCountryCode("py");

        FiscalProvider provider = configuration.fiscalProvider(
                properties,
                List.of(new FixedCountryFactory("PY", "PY-SIFEN")));

        assertThat(provider.providerCode()).isEqualTo("PY-SIFEN");
    }

    @Test
    void throwsWhenCountryCodeHasNoRegisteredProviderFactory() {
        FiscalProperties properties = new FiscalProperties();
        properties.setCountryCode("AR");

        assertThatThrownBy(() -> configuration.fiscalProvider(
                properties,
                List.of(new FixedCountryFactory("PY", "PY-SIFEN"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.fiscal.country-code=AR");
    }

    @Test
    void throwsWhenTwoFactoriesShareSameCountryCode() {
        FiscalProperties properties = new FiscalProperties();
        properties.setCountryCode("PY");

        assertThatThrownBy(() -> configuration.fiscalProvider(
                properties,
                List.of(
                        new FixedCountryFactory("PY", "PY-SIFEN-A"),
                        new FixedCountryFactory("py", "PY-SIFEN-B"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duplicate fiscal provider factory for country code: PY");
    }

    private record FixedCountryFactory(String countryCode, String providerCode) implements CountryFiscalProviderFactory {

        @Override
        public FiscalProvider createProvider() {
            return new FixedFiscalProvider(providerCode);
        }
    }

    private record FixedFiscalProvider(String providerCode) implements FiscalProvider {

        @Override
        public FiscalProviderResult issueInvoice(FiscalIssueInvoiceCommand command) {
            return new FiscalProviderResult(true, "INV-TEST", "ok");
        }

        @Override
        public FiscalProviderResult cancelInvoice(FiscalCancelInvoiceCommand command) {
            return new FiscalProviderResult(true, "INV-TEST", "ok");
        }

        @Override
        public FiscalProviderResult issueCreditNote(FiscalIssueCreditNoteCommand command) {
            return new FiscalProviderResult(true, "CN-TEST", "ok");
        }
    }
}
