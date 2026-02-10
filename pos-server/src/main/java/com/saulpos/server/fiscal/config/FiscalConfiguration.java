package com.saulpos.server.fiscal.config;

import com.saulpos.core.fiscal.FiscalProvider;
import com.saulpos.server.fiscal.service.StubFiscalProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FiscalProperties.class)
public class FiscalConfiguration {

    @Bean
    @ConditionalOnMissingBean(FiscalProvider.class)
    public FiscalProvider fiscalProvider() {
        return new StubFiscalProvider();
    }
}
