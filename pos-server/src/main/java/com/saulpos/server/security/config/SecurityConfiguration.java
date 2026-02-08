package com.saulpos.server.security.config;

import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.authorization.SecurityAuthority;
import com.saulpos.server.security.filter.BearerTokenAuthenticationFilter;
import com.saulpos.server.security.web.ProblemAccessDeniedHandler;
import com.saulpos.server.security.web.ProblemAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter,
                                                   ProblemAuthenticationEntryPoint authenticationEntryPoint,
                                                   ProblemAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/metrics",
                                "/actuator/metrics/**",
                                "/error",
                                "/test/**")
                        .permitAll()
                        .requestMatchers("/api/sales/**")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.SALES_PROCESS))
                        .requestMatchers("/api/shifts/**")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.SALES_PROCESS))
                        .requestMatchers(HttpMethod.GET, "/api/catalog/products/lookup")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers(HttpMethod.GET, "/api/catalog/prices/resolve")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers(HttpMethod.POST, "/api/tax/preview")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers(HttpMethod.POST, "/api/receipts/allocate")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.SALES_PROCESS))
                        .requestMatchers("/api/discounts/**")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers("/api/promotions/**")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers("/api/loyalty/**")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers("/api/customers/**")
                        .hasAnyAuthority(
                                SecurityAuthority.permission(PermissionCodes.SALES_PROCESS),
                                SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers(HttpMethod.POST, "/api/catalog/products/*/open-price/validate")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.OPEN_PRICE_ENTRY))
                        .requestMatchers("/api/catalog/**")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers("/api/refunds/**")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.REFUND_PROCESS))
                        .requestMatchers("/api/inventory/**")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.INVENTORY_ADJUST))
                        .requestMatchers("/api/reports/**")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.REPORT_VIEW))
                        .requestMatchers("/api/identity/**", "/api/security/roles/**", "/api/security/permissions/catalog")
                        .hasAuthority(SecurityAuthority.permission(PermissionCodes.CONFIGURATION_MANAGE))
                        .requestMatchers("/api/security/me", "/api/security/permissions/current", "/api/auth/logout")
                        .authenticated()
                        .requestMatchers("/api/security/**")
                        .authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
