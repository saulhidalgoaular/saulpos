package com.saulpos.server.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private int maxFailedAttempts = 5;
    private int lockDurationMinutes = 15;
    private int accessTokenTtlMinutes = 30;
    private int refreshTokenTtlMinutes = 480;
}
