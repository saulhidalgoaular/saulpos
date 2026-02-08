package com.saulpos.server.security.filter;

import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.security.service.AccessTokenValidationResult;
import com.saulpos.server.security.service.AccessTokenValidationStatus;
import com.saulpos.server.security.service.AuthenticatedUser;
import com.saulpos.server.security.service.TokenSessionService;
import com.saulpos.server.security.web.ProblemAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenSessionService tokenSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) {
                AccessTokenValidationResult validationResult = tokenSessionService.validateAccessToken(token);
                if (validationResult.isAuthenticated()) {
                    setAuthentication(validationResult.user());
                } else {
                    SecurityContextHolder.clearContext();
                    request.setAttribute(
                            ProblemAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTR,
                            mapErrorCode(validationResult.status()));
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(AuthenticatedUser user) {
        List<SimpleGrantedAuthority> authorities = user.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private ErrorCode mapErrorCode(AccessTokenValidationStatus status) {
        return switch (status) {
            case EXPIRED -> ErrorCode.AUTH_TOKEN_EXPIRED;
            case INACTIVE -> ErrorCode.AUTH_ACCOUNT_DISABLED;
            default -> ErrorCode.AUTH_TOKEN_INVALID;
        };
    }
}
