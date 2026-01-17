package com.jobboard.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Configuration
public class LoggingConfig {

    @Bean
    public OncePerRequestFilter loggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                // Generate trace ID for request tracking
                String traceId = UUID.randomUUID().toString().substring(0, 8);
                MDC.put("traceId", traceId);

                // Add trace ID to response header
                response.setHeader("X-Trace-Id", traceId);

                try {
                    filterChain.doFilter(request, response);
                } finally {
                    // Clean up MDC
                    MDC.clear();
                }
            }
        };
    }
}