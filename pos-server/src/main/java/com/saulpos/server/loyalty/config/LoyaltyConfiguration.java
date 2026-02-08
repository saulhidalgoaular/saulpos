package com.saulpos.server.loyalty.config;

import com.saulpos.server.loyalty.service.LoyaltyProvider;
import com.saulpos.server.loyalty.service.StubLoyaltyProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoyaltyProperties.class)
public class LoyaltyConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoyaltyProvider.class)
    public LoyaltyProvider loyaltyProvider() {
        return new StubLoyaltyProvider();
    }
}
