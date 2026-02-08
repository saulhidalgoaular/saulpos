package com.saulpos.server.loyalty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.loyalty")
public class LoyaltyProperties {

    private boolean enabled = false;
}
