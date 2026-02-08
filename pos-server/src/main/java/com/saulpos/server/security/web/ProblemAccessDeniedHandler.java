package com.saulpos.server.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                ErrorCode.AUTH_FORBIDDEN.getStatus(),
                ErrorCode.AUTH_FORBIDDEN.getMessage());
        problemDetail.setTitle(ErrorCode.AUTH_FORBIDDEN.getMessage());
        problemDetail.setProperty("code", ErrorCode.AUTH_FORBIDDEN.getCode());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problemDetail.setProperty("correlationId", correlationId);
        }

        response.setStatus(ErrorCode.AUTH_FORBIDDEN.getStatus().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
