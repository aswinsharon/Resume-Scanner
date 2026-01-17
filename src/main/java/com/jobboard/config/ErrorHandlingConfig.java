package com.jobboard.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class ErrorHandlingConfig {

    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

                // Add custom timestamp format
                errorAttributes.put("timestamp", LocalDateTime.now());

                // Remove sensitive information in production
                errorAttributes.remove("trace");

                // Add custom error code if available
                Object status = errorAttributes.get("status");
                if (status != null) {
                    errorAttributes.put("code", getErrorCode((Integer) status));
                }

                return errorAttributes;
            }

            private String getErrorCode(Integer status) {
                return switch (status) {
                    case 400 -> "BAD_REQUEST";
                    case 401 -> "UNAUTHORIZED";
                    case 403 -> "FORBIDDEN";
                    case 404 -> "NOT_FOUND";
                    case 409 -> "CONFLICT";
                    case 422 -> "UNPROCESSABLE_ENTITY";
                    case 500 -> "INTERNAL_SERVER_ERROR";
                    case 503 -> "SERVICE_UNAVAILABLE";
                    default -> "HTTP_" + status;
                };
            }
        };
    }
}